package com.github.istin.dmtools.presale;

import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.common.model.IAttachment;
import com.github.istin.dmtools.common.utils.Log;
import com.github.istin.dmtools.documentation.DocumentationEditor;
import com.github.istin.dmtools.documentation.area.TicketAreaMapperViaConfluence;
import com.github.istin.dmtools.documentation.area.TicketDocumentationHistoryTrackerViaConfluence;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.pdf.PdfAsTrackerClient;
import com.github.istin.dmtools.pdf.model.PdfPageAsTicket;
import com.github.istin.dmtools.presale.model.StoryEstimation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreSaleSupport extends AbstractJob<PreSaleSupportParams> {

    @Override
    public void runJob(PreSaleSupportParams preSaleSupportParams) throws Exception {
        String confluenceRootPage = preSaleSupportParams.getConfluenceRootPage();
        String folderWithPdfAssets = preSaleSupportParams.getFolderWithPdfAssets();
        String prefix = "jai";
        PdfAsTrackerClient rfpInputTracker = new PdfAsTrackerClient(folderWithPdfAssets);
        BasicOpenAI openAI = new BasicOpenAI();
        PromptManager promptManager = new PromptManager();

        JAssistant jAssistant = new JAssistant(rfpInputTracker, null, openAI, promptManager);

        rfpInputTracker.searchAndPerform(ticket -> {
            List<? extends IAttachment> attachments = ticket.getAttachments();
            if (!attachments.isEmpty() && ticket.getSnapshotDescription() == null) {
                String pageSnapshot = jAssistant.combineTextAndImage(ticket.getDescription(), ticket.getPageSnapshot());
                rfpInputTracker.updatePageSnapshot(ticket, pageSnapshot);
            }

            //Identify key requirements, objectives, and constraints.
            jAssistant.identifyIsContentRelatedToRequirementsAndMarkViaLabel(prefix, ticket);
//                rfpInputTracker.deleteLabelInTicket(ticket, "jai_not_requirements");
//                rfpInputTracker.deleteLabelInTicket(ticket, "jai_requirements");

            //check if the content is related to Timeline
            jAssistant.identifyIsContentRelatedToTimelineAndMarkViaLabel(prefix, ticket);
            //check if the content is related to team setup or any other expenses
            jAssistant.identifyIsContentRelatedToTeamSetupAndMarkViaLabel(prefix, ticket);

            return false;
        }, null, new String[]{});

        BasicConfluence confluence = BasicConfluence.getInstance();
        TicketDocumentationHistoryTrackerViaConfluence ticketDocumentationHistoryTrackerViaConfluence = new TicketDocumentationHistoryTrackerViaConfluence(confluence);


        DocumentationEditor documentationEditor = new DocumentationEditor(jAssistant, rfpInputTracker, confluence, confluenceRootPage);


        Content rootContent = confluence.findContent(confluenceRootPage);

        //building project timeline
        rfpInputTracker.searchAndPerform(ticket -> {
            documentationEditor.extendDocumentationPageWithTicket(confluence, ticketDocumentationHistoryTrackerViaConfluence, ticket, confluenceRootPage + " Project Timeline", rootContent, new DocumentationEditor.DelegateToAI() {

                @Override
                public String askAI(String source) throws Exception {
                    return jAssistant.buildProjectTimeline(ticket, source);
                }
            });
            return false;
        }, "labels=" + prefix + "_" + JAssistant.LABEL_TIMELINE, new String[]{});

        //building Team Setup input and other expenses
        rfpInputTracker.searchAndPerform(ticket -> {
            documentationEditor.extendDocumentationPageWithTicket(confluence, ticketDocumentationHistoryTrackerViaConfluence, ticket, confluenceRootPage + " Team Setup and Expenses", rootContent, new DocumentationEditor.DelegateToAI() {

                @Override
                public String askAI(String source) throws Exception {
                    return jAssistant.buildTeamSetupAndLicenses(ticket, source);
                }
            });
            return false;
        }, "labels=" + prefix + "_" + JAssistant.LABEL_TEAM_SETUP, new String[]{});

        //building requirements overview
        String rootRequirementsPageName = confluenceRootPage + " Requirements";
        confluence.findOrCreate(rootRequirementsPageName, rootContent.getId(), "");

        JSONObject jsonObject = documentationEditor.buildExistingAreasStructureForConfluence(confluenceRootPage, rootRequirementsPageName);
        Set<String> keys = jsonObject.keySet();
        TicketAreaMapperViaConfluence ticketAreaMapper = new TicketAreaMapperViaConfluence(confluenceRootPage, rootRequirementsPageName, confluence);
        List<PdfPageAsTicket> dataInputTicketsWithRequirements = rfpInputTracker.searchAndPerform("labels=" + prefix + "_" + JAssistant.LABEL_REQUIREMENTS, new String[]{});
        if (keys.isEmpty()) {
            buildKeyAreas(rootRequirementsPageName, confluence, rfpInputTracker, documentationEditor, dataInputTicketsWithRequirements, ticketAreaMapper);
        }

        documentationEditor.buildDetailedPageWithRequirementsForInputData(dataInputTicketsWithRequirements, confluenceRootPage, rootRequirementsPageName, confluence, ticketAreaMapper, ticketDocumentationHistoryTrackerViaConfluence, false);

        Set<String> pagesWithContent = getPages(documentationEditor, confluenceRootPage);

        List<StoryEstimation> estimations = new ArrayList<>();

        for (String pageWithContent : pagesWithContent) {
            Content content = confluence.findContent(pageWithContent);
            String wikiContent = content.getStorage().getValue();

            try {
                List<StoryEstimation> parse = PresaleResponseParser.parse(jAssistant.getEstimationInManHours(wikiContent));
                estimations.addAll(parse);
            } catch (Exception e) {
                System.out.println("PreSaleSupport Not able to estimate wiki page " + pageWithContent);
                Log.e("PreSaleSupport", e);
            }
        }

        PresaleResultExcelExporter.exportToExcel(estimations, preSaleSupportParams.getFolderWithPdfAssets());

        //TODO Analyze the requirements to determine the most efficient technology stack (e.g., React Native, Flutter for cross-platform development, or native iOS and Android).
        //TODO Recommended technology stack with justifications.

        //TODO Assess the potential challenges and draft mitigation strategies.
        //TODO List of potential challenges with mitigation strategies.

        //TODO Develop a comprehensive project plan detailing the milestones, tasks, and timelines.
        //TODO Include considerations for development, testing, deployment, and post-launch support.
//                Deliverables:
//
//                High-level project timeline.
//                Milestones and deliverables schedule.
//                Resource allocation and team composition.

                /*
                 Budget Estimation
                    Action:

                    Collaborate with the finance team to draft a budget estimate covering development, testing, deployment, and potential contingencies.
                    Ensure the estimate aligns with standard industry practices and the complexity of the project.
                    Deliverables:

                    Detailed budget overview.
                    Cost breakdown by phases and activities.
                    7. Prepare the Proposal Document

                    Action:

                    Consolidate all the gathered information into a professional proposal document.
                    Address each point specifically requested in the RFP (technology stack, challenges, implementation recommendations, timeline, team composition, and budget).
                    Structure:

                    Executive Summary
                    Understanding of Requirements
                    Proposed Solution
                    Technology Stack
                    Functional Implementation
                    Project Plan and Timeline
                    Project Team Composition
                    Roles and Responsibilities
                    Budget Overview
                    Cost Breakdown
                    Risk Mitigation Strategies
                    Conclusion and Next Steps
                    8. Quality Assurance

                    Action:

                    Proofread the proposal document for errors and inconsistencies.
                    Ensure alignment with client requirements and expectations.
                 */
    }

    private static void buildKeyAreas(String rootRequirementsPageName, BasicConfluence confluence, PdfAsTrackerClient rfpInputTracker, DocumentationEditor documentationEditor, List<PdfPageAsTicket> dataInputTicketsWithRequirements, TicketAreaMapperViaConfluence ticketAreaMapper) throws Exception {
        JSONArray draftFeatureAreas = documentationEditor.buildDraftFeatureAreasByDataInput(dataInputTicketsWithRequirements);
        draftFeatureAreas = documentationEditor.cleanFeatureAreas(draftFeatureAreas);
        System.out.println(draftFeatureAreas);
        JSONObject optimizedFeatureAreas = documentationEditor.createFeatureAreasTree(draftFeatureAreas);
        System.out.println(optimizedFeatureAreas);
        documentationEditor.buildConfluenceStructure(optimizedFeatureAreas, dataInputTicketsWithRequirements, rootRequirementsPageName, confluence, ticketAreaMapper);
    }


    //TODO Reimplement to more efficient way
    private Set<String> getPages(DocumentationEditor documentationEditor, String rootPage) throws Exception {
        JSONObject page = documentationEditor.buildExistingAreasStructureForConfluence("", rootPage);

        Set<String> pageKeys = page.keySet();

        HashSet<String> result = new HashSet<>();

        if (pageKeys.isEmpty()) {
            result.add(rootPage);

            return result;
        }

        for (String key : pageKeys) {
            JSONObject inner = page.getJSONObject(key);

            Set<String> innerSet = inner.keySet();

            if (key.endsWith("History")) {
                continue;
            }

            if (innerSet.size() == 1 && innerSet.iterator().next().equals(key + " History")) {
                result.add(key);
            } else {
                result.addAll(getPages(documentationEditor, key));
            }
        }

        return result;

    }

}