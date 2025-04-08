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
    } else {
        themeToggle.textContent = 'Switch to Dark Theme';
        updateAllCharts(false);
    }
});

// Print button functionality
document.getElementById('print-slides').addEventListener('click', function() {
    // Prepare for printing
    prepareForPrint();

    // Open print dialog
    window.print();

    // Restore after print dialog closes
    setTimeout(function() {
        restoreAfterPrint();
    }, 1000);
});

function prepareForPrint() {
    // Force light theme for printing
    const wasInDarkMode = document.body.classList.contains('dark-theme');
    if (wasInDarkMode) {
        document.body.classList.remove('dark-theme');
        updateAllCharts(false);
    }
    document.body.setAttribute('data-was-dark', wasInDarkMode);

    // Ensure all charts are properly sized for print
    Object.keys(charts).forEach(chartId => {
        const chart = charts[chartId];
        chart.resize();
    });
}

function restoreAfterPrint() {
    // Restore theme if it was dark before
    const wasInDarkMode = document.body.getAttribute('data-was-dark') === 'true';
    if (wasInDarkMode) {
        document.body.classList.add('dark-theme');
        updateAllCharts(true);
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

// Create a single slide based on type
function createSlide(slideData, index) {
    const slideDiv = document.createElement('div');
    slideDiv.className = `slide ${slideData.type}-slide`;

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
        default:
            slideContent = `<div class="slide-content-wrapper">
                <div class="slide-title">Unknown Slide Type</div>
            </div>`;
    }

    slideDiv.innerHTML = slideContent;
    return slideDiv;
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
</#noparse>