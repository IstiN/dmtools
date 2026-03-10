/**
 * Prepare Figma Artifacts (preCliJSAction)
 *
 * Extracts the Figma URL from the current ticket description, then fetches
 * all design data using the absolute minimum number of REST calls:
 *
 *   Call 1: figma_get_file_structure(href)   → full /v1/files/:key JSON (complete document tree)
 *   Call 2: figma_render_nodes(href, ids)     → batch /v1/images/:key?ids=… (all render URLs)
 *
 * Output: input/{TICKET-KEY}/figma_snapshot.json
 *         Contains the raw file structure enriched with render URLs per top-level node.
 *
 * Usage in job JSON:
 *   "preCliJSAction": "agents/js/prepareFigmaArtifacts.js"
 */
function action(params) {
    var ticket   = params.ticket;
    var ticketKey = ticket.key;
    var inputFolder = 'input/' + ticketKey;

    // --- Extract Figma URL from ticket description ---
    var description = (ticket.fields && ticket.fields.description) ? ticket.fields.description : '';
    var figmaMatch = description.match(/https:\/\/www\.figma\.com\/(file|design)\/[^\s\)"']+/);
    if (!figmaMatch) {
        console.warn('[prepareFigmaArtifacts] No Figma URL found in ticket ' + ticketKey + ' description — skipping');
        return true; // don't block the CLI
    }
    var href = figmaMatch[0];
    console.log('[prepareFigmaArtifacts] Figma URL: ' + href);

    // CALL 1: Fetch the complete Figma file document in one large REST call.
    // Hits /v1/files/:key?geometry=paths&depth=2 — returns the full node tree.
    var fileStructureRaw = figma_get_file_structure(href);
    if (!fileStructureRaw) {
        console.error('[prepareFigmaArtifacts] figma_get_file_structure returned null — aborting');
        return true;
    }
    var fileDoc = JSON.parse(fileStructureRaw);

    // Collect top-level node IDs from the document canvas pages
    var nodeIds = [];
    try {
        var pages = fileDoc.document && fileDoc.document.children ? fileDoc.document.children : [];
        pages.forEach(function(page) {
            var frames = page.children || [];
            frames.forEach(function(frame) {
                if (frame.id) nodeIds.push(frame.id);
            });
        });
    } catch (e) {
        console.warn('[prepareFigmaArtifacts] Could not extract node IDs from document: ' + e);
    }

    // CALL 2: Batch-render ALL top-level nodes in one request.
    // Hits /v1/images/:key?ids=id1,id2,… — auto-batches up to 100 IDs per request.
    var renderUrls = {};
    if (nodeIds.length > 0) {
        var renderRaw = figma_render_nodes(href, nodeIds.join(','), 'png');
        if (renderRaw) {
            renderUrls = JSON.parse(renderRaw);
        }
    }

    var snapshot = {
        href:        href,
        ticketKey:   ticketKey,
        fetchedAt:   new Date().toISOString(),
        renderUrls:  renderUrls,
        fileStructure: fileDoc
    };

    file_write(inputFolder + '/figma_snapshot.json', JSON.stringify(snapshot, null, 2));
    console.log('[prepareFigmaArtifacts] Saved ' + nodeIds.length + ' nodes (' + Object.keys(renderUrls).length + ' render URLs) → ' + inputFolder + '/figma_snapshot.json');

    return true;
}
