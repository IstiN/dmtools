<#noparse>
// Style and theme configuration
const CHART_STYLES = {
    light: {
        textColor: '#212529',
        gridColor: '#e9ecef',
        backgroundColor: 'rgba(255, 255, 255, 0.8)'
    },
    dark: {
        textColor: '#e9ecef',
        gridColor: '#343a40',
        backgroundColor: 'rgba(30, 30, 30, 0.8)'
    }
};

const CHART_FONT_CONFIG = {
    title: {
        family: "'Montserrat', sans-serif",
        size: 12,
        weight: 'bold'
    },
    body: {
        family: "'Open Sans', sans-serif",
        size: 11
    },
    legend: {
        family: "'Montserrat', sans-serif",
        size: 10
    }
};

const TOOLTIP_CONFIG = {
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    padding: 8,
    cornerRadius: 4
};

// Configure marked for security and features
marked.setOptions({
    gfm: true,
    breaks: true,
    sanitize: false,
    smartLists: true,
    smartypants: true
});

// Store all charts for theme updates
const charts = {};

// Theme toggle functionality
const themeToggle = document.getElementById('theme-toggle');
themeToggle.addEventListener('click', function() {
    document.body.classList.toggle('dark-theme');
    if (document.body.classList.contains('dark-theme')) {
        themeToggle.textContent = 'Switch to Light Theme';
        updateAllCharts(true);
        // Update editor theme if editor is open
        if (jsonEditor) {
            updateEditorTheme();
        }
    } else {
        themeToggle.textContent = 'Switch to Dark Theme';
        updateAllCharts(false);
        // Update editor theme if editor is open
        if (jsonEditor) {
            updateEditorTheme();
        }
    }
    // Reinitialize mermaid diagrams with new theme
    initializeMermaidDiagrams();
});

// PDF generation and download functionality
document.getElementById('print-slides').addEventListener('click', function() {
    // Show loading overlay
    const loadingOverlay = document.getElementById('pdf-loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.classList.add('active');
    }

    // Change button text to show processing
    const printButton = document.getElementById('print-slides');
    const originalButtonText = printButton.textContent;
    printButton.textContent = 'Generating PDF...';
    printButton.disabled = true;

    // Force light theme for PDF generation
    const wasInDarkMode = document.body.classList.contains('dark-theme');
    if (wasInDarkMode) {
        document.body.classList.remove('dark-theme');
        updateAllCharts(false);
    }

    // Prepare mermaid diagrams and then generate PDF
    prepareMermaidDiagrams().then(() => {
        // Get all slides
        const slides = document.querySelectorAll('.slide');

        // Create PDF with landscape orientation
        const { jsPDF } = window.jspdf;
        const pdf = new jsPDF({
            orientation: 'landscape',
            unit: 'mm',
            format: 'a4'
        });

        // Hide edit buttons during PDF generation
        document.querySelectorAll('.edit-button').forEach(button => {
            button.style.display = 'none';
        });

        // Function to process each slide
        const processSlide = function(index) {
            if (index >= slides.length) {
                // All slides processed, download PDF
                pdf.save('presentation.pdf');

                // Restore theme if needed
                if (wasInDarkMode) {
                    document.body.classList.add('dark-theme');
                    updateAllCharts(true);
                    initializeMermaidDiagrams();
                }

                // Show edit buttons again
                document.querySelectorAll('.edit-button').forEach(button => {
                    button.style.display = '';
                });

                // Restore button
                printButton.textContent = originalButtonText;
                printButton.disabled = false;

                // Hide loading overlay
                if (loadingOverlay) {
                    loadingOverlay.classList.remove('active');
                }

                return;
            }

            const slide = slides[index];

            // Capture the slide as an image
            html2canvas(slide, {
                scale: 2, // Higher resolution
                useCORS: true, // Allow cross-origin images
                allowTaint: true,
                backgroundColor: '#ffffff',
                logging: false
            }).then(function(canvas) {
                // Calculate dimensions to fit the slide properly on the PDF page
                const imgData = canvas.toDataURL('image/png');
                const pageWidth = pdf.internal.pageSize.getWidth();
                const pageHeight = pdf.internal.pageSize.getHeight();

                const imgWidth = pageWidth - 20; // 10mm margin on each side
                const imgHeight = (canvas.height * imgWidth) / canvas.width;

                // Add new page if not the first slide
                if (index > 0) {
                    pdf.addPage();
                }

                // Add the image centered on the page
                pdf.addImage(
                    imgData,
                    'PNG',
                    10, // x position (10mm from left)
                    (pageHeight - imgHeight) / 2, // y position (centered vertically)
                    imgWidth,
                    imgHeight
                );

                // Process next slide
                setTimeout(function() {
                    processSlide(index + 1);
                }, 200);

            }).catch(function(error) {
                console.error('Error generating PDF for slide', index, error);

                // Continue with next slide even if there's an error
                setTimeout(function() {
                    processSlide(index + 1);
                }, 200);
            });
        };

        // Start processing slides
        processSlide(0);
    });
});

// Add this to your prepareForPrint function
// Prepare for printing - keep as synchronous function
function prepareForPrint() {
    // Force light theme for printing
    const wasInDarkMode = document.body.classList.contains('dark-theme');
    if (wasInDarkMode) {
        document.body.classList.remove('dark-theme');
        updateAllCharts(false);
        // Reinitialize mermaid diagrams with light theme
        initializeMermaidDiagrams();
    }
    document.body.setAttribute('data-was-dark', wasInDarkMode);

    // Ensure all charts are properly sized for print
    Object.keys(charts).forEach(chartId => {
        const chart = charts[chartId];
        chart.resize();
    });

    // Give mermaid diagrams time to render properly before printing
    setTimeout(() => {
        window.print();

        // Restore after print dialog closes
        setTimeout(function() {
            restoreAfterPrint();
        }, 1000);
    }, 500);
}

function restoreAfterPrint() {
    // Restore theme if it was dark before
    const wasInDarkMode = document.body.getAttribute('data-was-dark') === 'true';
    if (wasInDarkMode) {
        document.body.classList.add('dark-theme');
        updateAllCharts(true);
        // Reinitialize mermaid diagrams with dark theme
        initializeMermaidDiagrams();
    }
}

// Update all charts when theme changes
function updateAllCharts(isDark) {
    const theme = isDark ? CHART_STYLES.dark : CHART_STYLES.light;

    Object.keys(charts).forEach(chartId => {
        const chart = charts[chartId];
        if (chart.config.type === 'bar' || chart.config.type === 'line') {
            chart.options.scales.x.ticks.color = theme.textColor;
            chart.options.scales.y.ticks.color = theme.textColor;
            chart.options.scales.x.grid.color = theme.gridColor;
            chart.options.scales.y.grid.color = theme.gridColor;
        }
        chart.options.plugins.legend.labels.color = theme.textColor;
        chart.update();
    });
}

// JSON Editor variables
let jsonEditor = null;
let currentEditingSlideIndex = -1;

// Initialize JSON Editor
function initJsonEditor() {
    const container = document.getElementById('jsoneditor');
    const options = {
        mode: 'tree',
        modes: ['tree', 'view', 'form', 'code', 'text'],
        onError: function(err) {
            alert('JSON Error: ' + err.toString());
        },
        onModeChange: function(newMode, oldMode) {
            // Adjust height for different modes
            if (newMode === 'code' || newMode === 'text') {
                container.style.height = '100%';
            }
        }
    };

    jsonEditor = new JSONEditor(container, options);
}

// Show editor with slide data and animation
// Show editor with slide data and animation
function showEditor(slideIndex) {
    const editorOverlay = document.getElementById('editor-overlay');
    currentEditingSlideIndex = slideIndex;

    // Set the editor content
    jsonEditor.set(presentationData.slides[slideIndex]);

    // Clear the AI prompt input
    document.getElementById('ai-prompt').value = '';

    // Show the editor with animation
    editorOverlay.style.display = 'flex';
    editorOverlay.style.opacity = '0';

    // Force reflow to ensure animation works
    void editorOverlay.offsetWidth;

    // Fade in
    editorOverlay.style.opacity = '1';

    // Apply theme to editor
    updateEditorTheme();
}

// Add keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // If editor is open
    if (document.getElementById('editor-overlay').style.display === 'flex') {
        // Escape key closes the editor
        if (e.key === 'Escape') {
            closeEditor();
        }

        // Ctrl+Enter or Cmd+Enter to save changes
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            saveSlideChanges();
        }

        // Alt+A to trigger Ask AI
        if (e.altKey && e.key === 'a') {
            askAI();
        }
    }
});

// Cycle through AI prompt suggestions
function setupAIPromptPlaceholders() {
    const promptInput = document.getElementById('ai-prompt');
    const suggestions = [
        "Improve the title of this slide",
        "Make the content more engaging",
        "Suggest better colors for the chart",
        "Simplify the language in this slide",
        "Add a compelling conclusion",
        "Make this slide more visually appealing",
        "Suggest data points to highlight",
        "Rewrite this in a more professional tone",
        "Add relevant bullet points to this slide"
    ];

    let currentIndex = 0;

    // Change placeholder every 5 seconds
    setInterval(() => {
        promptInput.setAttribute('placeholder', suggestions[currentIndex]);
        currentIndex = (currentIndex + 1) % suggestions.length;
    }, 5000);
}


// Close the editor with animation
function closeEditor() {
    const editorOverlay = document.getElementById('editor-overlay');

    // Fade out
    editorOverlay.style.opacity = '0';

    // Hide after animation completes
    setTimeout(() => {
        editorOverlay.style.display = 'none';
        currentEditingSlideIndex = -1;
    }, 300);
}

// Update editor theme based on current page theme
function updateEditorTheme() {
    if (document.body.classList.contains('dark-theme')) {
        document.querySelector('.jsoneditor').classList.add('jsoneditor-dark');
    } else {
        document.querySelector('.jsoneditor').classList.remove('jsoneditor-dark');
    }
}

// Save changes from editor
function saveSlideChanges() {
    try {
        // Get the edited JSON
        const updatedSlideData = jsonEditor.get();

        // Update the presentation data
        presentationData.slides[currentEditingSlideIndex] = updatedSlideData;

        // Regenerate the presentation
        generatePresentation(presentationData);

        // Close the editor
        closeEditor();

        // Show success message
        showNotification('Slide updated successfully!');
    } catch (err) {
        alert('Error saving changes: ' + err.toString());
    }
}

// Show notification
function showNotification(message) {
    // Create notification element if it doesn't exist
    let notification = document.getElementById('notification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'notification';
        notification.className = 'notification';
        document.body.appendChild(notification);
    }

    // Set message and show
    notification.textContent = message;
    notification.classList.add('show');

    // Hide after 3 seconds
    setTimeout(() => {
        notification.classList.remove('show');
    }, 3000);
}

function createSlide(slideData, index) {
    const slideDiv = document.createElement('div');
    slideDiv.className = `slide ${slideData.type}-slide`;
    slideDiv.id = `slide-${index}`;

    // Add edit button with SVG icon
    const editButton = document.createElement('button');
    editButton.className = 'edit-button';
    editButton.title = 'Edit slide';
    editButton.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
    `;
    editButton.onclick = function(e) {
        e.stopPropagation();
        showEditor(index);
    };

    let slideContent = '';

    switch(slideData.type) {
        case 'title':
            slideContent = createTitleSlide(slideData);
            break;
        case 'content':
            slideContent = createContentSlide(slideData);
            break;
        case 'table':
            slideContent = createTableSlide(slideData);
            break;
        case 'bar-chart':
            slideContent = createBarChartSlide(slideData, index);
            break;
        case 'pie-chart':
            slideContent = createPieChartSlide(slideData, index);
            break;
        case 'image':
            slideContent = createImageSlide(slideData);
            break;
        case 'mermaid':
            slideContent = createMermaidSlide(slideData);
            break;
        default:
            slideContent = `<div class="slide-content-wrapper">
                <div class="slide-title">Unknown Slide Type</div>
            </div>`;
    }

    slideDiv.innerHTML = slideContent;
    slideDiv.appendChild(editButton);
    return slideDiv;
}

// Initialize the editor and AI functionality
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the JSON editor
    initJsonEditor();

    // Close editor button
    document.getElementById('close-editor').addEventListener('click', closeEditor);

    // Save slide button
    document.getElementById('save-slide').addEventListener('click', saveSlideChanges);

    // Ask AI button
    document.getElementById('ask-ai-button').addEventListener('click', askAI);

    // Close editor when clicking outside
    document.getElementById('editor-overlay').addEventListener('click', function(e) {
        if (e.target === this) {
            closeEditor();
        }
    });

    // Add notification styles
    const style = document.createElement('style');
    style.textContent = `
        .notification {
            position: fixed;
            bottom: 30px;
            left: 50%;
            transform: translateX(-50%) translateY(100px);
            background-color: var(--accent-color);
            color: white;
            padding: 14px 24px;
            border-radius: 8px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
            z-index: 1100;
            transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            font-family: 'Montserrat', sans-serif;
            font-weight: 500;
            display: flex;
            align-items: center;
            min-width: 300px;
            justify-content: center;
        }

        .notification.show {
            transform: translateX(-50%) translateY(0);
        }

        .notification::before {
            content: "✓";
            margin-right: 10px;
            font-weight: bold;
            font-size: 1.2em;
        }
    `;
    document.head.appendChild(style);

    // Update editor theme when page theme changes
    themeToggle.addEventListener('click', function() {
        if (jsonEditor) {
            setTimeout(updateEditorTheme, 100);
        }
    });

    setupAIPromptPlaceholders();
});

// Ask AI function
function askAI() {
    // Get the current slide data from the editor
    const slideData = jsonEditor.get();

    // Get the prompt from the input field
    const promptInput = document.getElementById('ai-prompt');
    const prompt = promptInput.value.trim();

    if (!prompt) {
        alert("Please enter a prompt for the AI.");
        return;
    }

    // Create the data to send to AI
    const aiRequestData = {
        slideData: slideData,
        prompt: prompt
    };

    // For now, just show an alert with the data
    alert("AI Request:\n\nPrompt: " + prompt + "\n\nSlide Data: " + JSON.stringify(slideData, null, 2));

    // Clear the input field
    promptInput.value = '';

    // In a real implementation, you would send this to your backend
    // sendToAIService(aiRequestData);
}

// Example of how you might implement the AI service call
function sendToAIService(data) {
    // This is a placeholder for the actual implementation
    // You would use fetch or another method to call your backend

    fetch('/api/ai/assist', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(result => {
        // Handle the AI response
        if (result.updatedSlide) {
            // Update the editor with the AI's suggestions
            jsonEditor.set(result.updatedSlide);
            showNotification("AI suggestions applied!");
        }
    })
    .catch(error => {
        console.error('Error calling AI service:', error);
        alert('Error calling AI service. Please try again.');
    });
}

// Create title slide
function createTitleSlide(data) {
    return `
        <div class="slide-content-wrapper">
            <div class="slide-title">${escapeHtml(data.title)}</div>
            <div class="slide-subtitle">${escapeHtml(data.subtitle || '')}</div>
            <div class="presenter-info">
                <p>Presented by: ${escapeHtml(data.presenter || '')}</p>
                ${data.presenterTitle ? `<p>${escapeHtml(data.presenterTitle)}</p>` : ''}
                ${data.date ? `<p>${escapeHtml(data.date)}</p>` : ''}
            </div>
        </div>
    `;
}

// Create content slide
function createContentSlide(data) {
    return `
        <div class="slide-content-wrapper">
            <div class="slide-header">
                <div class="slide-title">${escapeHtml(data.title)}</div>
                ${data.subtitle ? `<div class="slide-subtitle">${escapeHtml(data.subtitle)}</div>` : ''}
            </div>

            ${data.description ? createDescriptionBlock(data.description) : ''}

            <div class="slide-content">
                <div class="markdown-content">
                    ${marked.parse(data.content || '')}
                </div>
            </div>
        </div>
    `;
}

// Create table slide
function createTableSlide(data) {
    const tableHtml = createTableHtml(data.tableData);

    return `
        <div class="slide-content-wrapper">
            <div class="slide-header">
                <div class="slide-title">${escapeHtml(data.title)}</div>
                ${data.subtitle ? `<div class="slide-subtitle">${escapeHtml(data.subtitle)}</div>` : ''}
            </div>

            ${data.description ? createDescriptionBlock(data.description) : ''}

            <div class="slide-content">
                ${tableHtml}
            </div>
        </div>
    `;
}

// Create bar chart slide
function createBarChartSlide(data, index) {
    const chartId = `barChart-${index}`;

    return `
        <div class="slide-content-wrapper">
            <div class="slide-header">
                <div class="slide-title">${escapeHtml(data.title)}</div>
                ${data.subtitle ? `<div class="slide-subtitle">${escapeHtml(data.subtitle)}</div>` : ''}
            </div>

            ${data.description ? createDescriptionBlock(data.description) : ''}

            <div class="slide-content">
                <div class="chart-container">
                    <canvas id="${chartId}"></canvas>
                </div>
            </div>
        </div>
    `;
}

// Create pie chart slide
function createPieChartSlide(data, index) {
    const chartId = `pieChart-${index}`;
    const metricsHtml = createMetricsHtml(data.metrics || []);

    return `
        <div class="slide-content-wrapper">
            <div class="slide-header">
                <div class="slide-title">${escapeHtml(data.title)}</div>
                ${data.subtitle ? `<div class="slide-subtitle">${escapeHtml(data.subtitle)}</div>` : ''}
            </div>

            ${data.description ? createDescriptionBlock(data.description) : ''}

            <div class="slide-content">
                <div class="slide-text">
                    ${data.insights ? `<div class="markdown-content">${marked.parse(data.insights)}</div>` : ''}

                    ${metricsHtml ? `<div class="key-metrics">${metricsHtml}</div>` : ''}
                </div>

                <div class="pie-chart-container">
                    <div class="pie-chart-wrapper">
                        <canvas id="${chartId}"></canvas>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Create image slide
function createImageSlide(data) {
    return `
        <div class="slide-content-wrapper">
            <div class="slide-header">
                <div class="slide-title">${escapeHtml(data.title)}</div>
                ${data.subtitle ? `<div class="slide-subtitle">${escapeHtml(data.subtitle)}</div>` : ''}
            </div>

            ${data.description ? createDescriptionBlock(data.description) : ''}

            <div class="slide-content">
                ${data.imageUrl ?
                    `<img src="${escapeHtml(data.imageUrl)}" alt="${escapeHtml(data.caption || 'Slide image')}" class="slide-image">` :
                    `<div class="image-placeholder"><p>[Image Placeholder]</p></div>`
                }
                ${data.caption ? `<p class="image-caption">${escapeHtml(data.caption)}</p>` : ''}
            </div>
        </div>
    `;
}

// Create description block
function createDescriptionBlock(description) {
    if (!description) return '';

    let bulletHtml = '';
    if (description.bullets && description.bullets.length > 0) {
        bulletHtml = '<ul class="description-bullets">';
        description.bullets.forEach(bullet => {
            bulletHtml += `<li>${escapeHtml(bullet)}</li>`;
        });
        bulletHtml += '</ul>';
    }

    return `
        <div class="slide-description">
            ${description.title ? `<div class="description-title">${escapeHtml(description.title)}</div>` : ''}
            ${description.text ? `<div class="description-text">${escapeHtml(description.text)}</div>` : ''}
            ${bulletHtml}
        </div>
    `;
}

// Create table HTML
function createTableHtml(tableData) {
    if (!tableData || !tableData.headers || !tableData.rows) {
        return '<p>No table data available</p>';
    }

    let tableHtml = '<table>';

    // Headers
    tableHtml += '<thead><tr>';
    tableData.headers.forEach(header => {
        tableHtml += `<th>${escapeHtml(header)}</th>`;
    });
    tableHtml += '</tr></thead>';

    // Rows
    tableHtml += '<tbody>';
    tableData.rows.forEach(row => {
        tableHtml += '<tr>';
        row.forEach(cell => {
            tableHtml += `<td>${escapeHtml(cell)}</td>`;
        });
        tableHtml += '</tr>';
    });
    tableHtml += '</tbody>';

    tableHtml += '</table>';
    return tableHtml;
}

// Create metrics HTML
function createMetricsHtml(metrics) {
    if (!metrics || metrics.length === 0) return '';

    let metricsHtml = '';
    metrics.forEach(metric => {
        metricsHtml += `
            <div class="metric">
                <span class="metric-value">${escapeHtml(metric.value)}</span>
                <span class="metric-label">${escapeHtml(metric.label)}</span>
            </div>
        `;
    });

    return metricsHtml;
}

// Initialize charts after DOM is ready
function initializeCharts() {
    // Find all bar charts
    document.querySelectorAll('[id^="barChart-"]').forEach(canvas => {
        const index = canvas.id.split('-')[1];
        const slideData = presentationData.slides[index];

        if (slideData && slideData.chartData) {
            const ctx = canvas.getContext('2d');
            const isDark = document.body.classList.contains('dark-theme');
            const theme = isDark ? CHART_STYLES.dark : CHART_STYLES.light;

            const chart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: slideData.chartData.labels,
                    datasets: slideData.chartData.datasets.map(dataset => ({
                        ...dataset,
                        borderWidth: 1
                    }))
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                color: theme.textColor,
                                font: { size: CHART_FONT_CONFIG.body.size }
                            },
                            grid: { color: theme.gridColor }
                        },
                        x: {
                            ticks: {
                                color: theme.textColor,
                                font: { size: CHART_FONT_CONFIG.body.size }
                            },
                            grid: { color: theme.gridColor }
                        }
                    },
                    plugins: {
                        legend: {
                            labels: {
                                color: theme.textColor,
                                font: {
                                    family: CHART_FONT_CONFIG.legend.family,
                                    size: CHART_FONT_CONFIG.legend.size
                                },
                                boxWidth: 12
                            },
                            position: 'top'
                        },
                        tooltip: {
                            backgroundColor: TOOLTIP_CONFIG.backgroundColor,
                            titleFont: {
                                family: CHART_FONT_CONFIG.title.family,
                                size: CHART_FONT_CONFIG.title.size,
                                weight: CHART_FONT_CONFIG.title.weight
                            },
                            bodyFont: {
                                family: CHART_FONT_CONFIG.body.family,
                                size: CHART_FONT_CONFIG.body.size
                            },
                            padding: TOOLTIP_CONFIG.padding,
                            cornerRadius: TOOLTIP_CONFIG.cornerRadius
                        }
                    }
                }
            });

            charts[canvas.id] = chart;
        }
    });

    // Find all pie charts
    document.querySelectorAll('[id^="pieChart-"]').forEach(canvas => {
        const index = canvas.id.split('-')[1];
        const slideData = presentationData.slides[index];

        if (slideData && slideData.chartData) {
            const ctx = canvas.getContext('2d');
            const isDark = document.body.classList.contains('dark-theme');
            const theme = isDark ? CHART_STYLES.dark : CHART_STYLES.light;

            const chart = new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: slideData.chartData.labels,
                    datasets: [{
                        data: slideData.chartData.data,
                        backgroundColor: slideData.chartData.backgroundColor,
                        borderColor: slideData.chartData.backgroundColor.map(color =>
                            color.replace('0.8', '1')
                        ),
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: theme.textColor,
                                font: {
                                    family: CHART_FONT_CONFIG.legend.family,
                                    size: CHART_FONT_CONFIG.legend.size
                                },
                                padding: 10,
                                boxWidth: 10
                            }
                        },
                        tooltip: {
                            backgroundColor: TOOLTIP_CONFIG.backgroundColor,
                            titleFont: {
                                family: CHART_FONT_CONFIG.title.family,
                                size: CHART_FONT_CONFIG.title.size,
                                weight: CHART_FONT_CONFIG.title.weight
                            },
                            bodyFont: {
                                family: CHART_FONT_CONFIG.body.family,
                                size: CHART_FONT_CONFIG.body.size
                            },
                            padding: TOOLTIP_CONFIG.padding,
                            cornerRadius: TOOLTIP_CONFIG.cornerRadius,
                            callbacks: {
                                label: function(context) {
                                    return `${context.label}: ${context.raw}%`;
                                }
                            }
                        }
                    }
                }
            });

            charts[canvas.id] = chart;
        }
    });
}

// Helper function to escape HTML
function escapeHtml(unsafe) {
    if (unsafe === undefined || unsafe === null) return '';
    return unsafe
        .toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Generate the entire presentation
function generatePresentation(data) {
    const slidesContainer = document.getElementById('slides-container');
    slidesContainer.innerHTML = '';

    // Clear existing charts
    Object.keys(charts).forEach(key => delete charts[key]);

    // Create slides
    data.slides.forEach((slide, index) => {
        const slideElement = createSlide(slide, index);
        slidesContainer.appendChild(slideElement);
    });

    // Initialize charts after DOM is updated
    setTimeout(() => {
        initializeCharts();
        initializeMermaidDiagrams();
    }, 100);
}

// Generate presentation on page load
document.addEventListener('DOMContentLoaded', function() {
    generatePresentation(presentationData);
});

// Handle beforeprint event to ensure charts are properly rendered
window.addEventListener('beforeprint', function() {
    prepareForPrint();
});

// Handle afterprint event to restore the UI
window.addEventListener('afterprint', function() {
    restoreAfterPrint();
});

// Show notification
function showNotification(message) {
    // Create notification element if it doesn't exist
    let notification = document.getElementById('notification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'notification';
        notification.className = 'notification';
        document.body.appendChild(notification);
    }

    // Set message and show
    notification.textContent = message;
    notification.classList.add('show');

    // Hide after 3 seconds
    setTimeout(() => {
        notification.classList.remove('show');

        // Remove from DOM after animation completes
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 400);
    }, 3000);
}

// Create mermaid diagram slide
function createMermaidSlide(data) {
    const diagramId = `mermaid-diagram-${Math.random().toString(36).substring(2, 11)}`;

    return `
        <div class="slide-content-wrapper">
            <div class="slide-header">
                <div class="slide-title">${escapeHtml(data.title)}</div>
                ${data.subtitle ? `<div class="slide-subtitle">${escapeHtml(data.subtitle)}</div>` : ''}
            </div>

            ${data.description ? createDescriptionBlock(data.description) : ''}

            <div class="slide-content">
                <div class="mermaid-container">
                    <div class="mermaid" id="${diagramId}">
                        ${data.diagramCode || ''}
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Initialize mermaid diagrams
function initializeMermaidDiagrams() {
    // Configure mermaid based on current theme
    const isDark = document.body.classList.contains('dark-theme');

    mermaid.initialize({
        startOnLoad: true,
        theme: isDark ? 'dark' : 'default',
        securityLevel: 'loose',
        fontFamily: "'Open Sans', sans-serif",
        fontSize: 14,
        flowchart: {
            htmlLabels: true,
            curve: 'basis'
        },
        themeVariables: isDark ? {
            primaryColor: '#4cc9f0',
            primaryTextColor: '#e9ecef',
            primaryBorderColor: '#4cc9f0',
            lineColor: '#adb5bd',
            secondaryColor: '#3a0ca3',
            tertiaryColor: '#2d3748'
        } : {
            primaryColor: '#4361ee',
            primaryTextColor: '#212529',
            primaryBorderColor: '#4361ee',
            lineColor: '#495057',
            secondaryColor: '#4895ef',
            tertiaryColor: '#f5f7ff'
        }
    });

    // Render all mermaid diagrams
    mermaid.run();
}

function prepareMermaidDiagrams() {
    // Force re-render of all mermaid diagrams
    mermaid.initialize({
        startOnLoad: true,
        theme: 'default',
        securityLevel: 'loose'
    });

    // Return a promise that resolves when diagrams are rendered
    return new Promise(resolve => {
        mermaid.run().then(() => {
            setTimeout(resolve, 300);
        });
    });
}
</#noparse>