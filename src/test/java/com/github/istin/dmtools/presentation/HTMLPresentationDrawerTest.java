package com.github.istin.dmtools.presentation;

import freemarker.template.TemplateException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class HTMLPresentationDrawerTest {

    private HTMLPresentationDrawer htmlPresentationDrawer;

    @Before
    public void setUp() {
        htmlPresentationDrawer = new HTMLPresentationDrawer();
    }

    @Test
    public void testPrintPresentation() throws TemplateException, IOException {
        // Arrange
        String topic = "TestTopic";
        JSONObject presentation = createSamplePresentation();

        // Act
        File result = htmlPresentationDrawer.printPresentation(topic, presentation);

        // Assert
        assertNotNull(result);
        assertTrue(result.exists());
        assertEquals(topic + "_presentation.html", result.getName());

        // Verify file content contains expected elements
        String content = new String(Files.readAllBytes(result.toPath()));
        assertTrue("File should contain presentation title",
                content.contains("Sample Presentation"));
        assertTrue("File should contain presenter name",
                content.contains("Test Presenter"));

        // Clean up
        result.delete();
    }

    @Test
    public void testPrintPresentationWithAllSlideTypes() throws TemplateException, IOException {
        // Arrange
        String topic = "ComprehensiveTest";
        JSONObject presentation = createComprehensivePresentation();

        // Act
        File result = htmlPresentationDrawer.printPresentation(topic, presentation);

        // Assert
        assertNotNull(result);
        assertTrue(result.exists());
        assertEquals(topic + "_presentation.html", result.getName());

        // Verify file content contains expected elements from different slide types
        String content = new String(Files.readAllBytes(result.toPath()));

        // Title slide elements
        assertTrue("File should contain title slide",
                content.contains("Comprehensive Presentation"));
        assertTrue("File should contain presenter info",
                content.contains("Expert Presenter"));

        // Content slide elements
        assertTrue("File should contain content slide",
                content.contains("Key Technologies"));
        assertTrue("File should contain markdown content",
                content.contains("Machine Learning"));

        // Table slide elements
        assertTrue("File should contain table slide",
                content.contains("Quarterly Results"));
        assertTrue("File should contain table data",
                content.contains("22.6M"));

        // Bar chart slide elements
        assertTrue("File should contain bar chart slide",
                content.contains("AI Market Growth"));
        assertTrue("File should contain chart data",
                content.contains("93.5"));

        // Pie chart slide elements
        assertTrue("File should contain pie chart slide",
                content.contains("Market Share"));
        assertTrue("File should contain pie chart data",
                content.contains("Company A"));

        // Image slide elements
        assertTrue("File should contain image slide",
                content.contains("Visual Reference"));
        assertTrue("File should contain image caption",
                content.contains("System architecture diagram"));

        // Add this to your testPrintPresentationWithAllSlideTypes method
        // Mermaid diagram slide elements
        assertTrue("File should contain mermaid diagram slide",
                content.contains("User Flow Diagram"));
        assertTrue("File should contain mermaid diagram code",
                content.contains("graph LR"));
        // Clean up
        //result.delete();
    }

    private JSONObject createSamplePresentation() {
        // Create a basic sample presentation JSON structure
        JSONObject presentation = new JSONObject();
        presentation.put("title", "Sample Presentation");

        JSONArray slides = new JSONArray();

        // Title slide
        JSONObject titleSlide = new JSONObject();
        titleSlide.put("type", "title");
        titleSlide.put("title", "Sample Presentation");
        titleSlide.put("subtitle", "Presentation for Test Audience");
        titleSlide.put("presenter", "Test Presenter");
        titleSlide.put("presenterTitle", "Test Title");
        titleSlide.put("date", "May 2023");
        slides.put(titleSlide);

        // Content slide
        JSONObject contentSlide = new JSONObject();
        contentSlide.put("type", "content");
        contentSlide.put("title", "Sample Content");
        contentSlide.put("subtitle", "Important Information");

        JSONObject description = new JSONObject();
        description.put("title", "Key Points");
        description.put("text", "This is sample text for the presentation");

        JSONArray bullets = new JSONArray();
        bullets.put("Point 1");
        bullets.put("Point 2");
        bullets.put("Point 3");
        description.put("bullets", bullets);

        contentSlide.put("description", description);
        contentSlide.put("content", "## Sample Content\n\n* Bullet 1\n* Bullet 2");
        slides.put(contentSlide);

        // Conclusion slide
        JSONObject conclusionSlide = new JSONObject();
        conclusionSlide.put("type", "content");
        conclusionSlide.put("title", "Conclusion");
        conclusionSlide.put("subtitle", "Key Takeaways");
        conclusionSlide.put("content", "## Thank You\n\n* Questions?\n* Comments?");
        slides.put(conclusionSlide);

        presentation.put("slides", slides);
        return presentation;
    }

    private JSONObject createComprehensivePresentation() {
        // Create a comprehensive presentation with all slide types from the prompt
        JSONObject presentation = new JSONObject();
        presentation.put("title", "Comprehensive Presentation");

        JSONArray slides = new JSONArray();

        // 1. Title slide
        JSONObject titleSlide = new JSONObject();
        titleSlide.put("type", "title");
        titleSlide.put("title", "Comprehensive Presentation");
        titleSlide.put("subtitle", "Showcasing All Slide Types");
        titleSlide.put("presenter", "Expert Presenter");
        titleSlide.put("presenterTitle", "Senior Presentation Designer");
        titleSlide.put("date", "June 2023");
        slides.put(titleSlide);

        // 2. Content slide
        JSONObject contentSlide = new JSONObject();
        contentSlide.put("type", "content");
        contentSlide.put("title", "Key Technologies");
        contentSlide.put("subtitle", "Core Technologies");

        JSONObject contentDescription = new JSONObject();
        contentDescription.put("title", "Overview");
        contentDescription.put("text", "Key technologies driving AI innovation");

        JSONArray contentBullets = new JSONArray();
        contentBullets.put("Transforming industries");
        contentBullets.put("Enabling new capabilities");
        contentDescription.put("bullets", contentBullets);

        contentSlide.put("description", contentDescription);
        contentSlide.put("content", "## AI Technologies\n\n* **ML**: Machine Learning\n* **DL**: Deep Learning\n* **NLP**: Natural Language Processing\n* **CV**: Computer Vision\n* **RL**: Reinforcement Learning");
        slides.put(contentSlide);

        // 3. Table slide
        JSONObject tableSlide = new JSONObject();
        tableSlide.put("type", "table");
        tableSlide.put("title", "Quarterly Results");
        tableSlide.put("subtitle", "Annual Performance");

        JSONObject tableDescription = new JSONObject();
        tableDescription.put("title", "Performance");
        tableDescription.put("text", "Q1-Q4 performance metrics");

        JSONArray tableBullets = new JSONArray();
        tableBullets.put("Consistent improvement");
        tableBullets.put("Strong Q4 results");
        tableDescription.put("bullets", tableBullets);

        JSONObject tableData = new JSONObject();
        JSONArray headers = new JSONArray();
        headers.put("Quarter");
        headers.put("Revenue");
        headers.put("Growth");
        tableData.put("headers", headers);

        JSONArray rows = new JSONArray();
        JSONArray row1 = new JSONArray();
        row1.put("Q1");
        row1.put("12.4M");
        row1.put("+8%");
        rows.put(row1);

        JSONArray row2 = new JSONArray();
        row2.put("Q2");
        row2.put("14.8M");
        row2.put("+19%");
        rows.put(row2);

        JSONArray row3 = new JSONArray();
        row3.put("Q3");
        row3.put("18.2M");
        row3.put("+23%");
        rows.put(row3);

        JSONArray row4 = new JSONArray();
        row4.put("Q4");
        row4.put("22.6M");
        row4.put("+24%");
        rows.put(row4);

        tableData.put("rows", rows);
        tableSlide.put("tableData", tableData);
        tableSlide.put("description", tableDescription);
        slides.put(tableSlide);

        // 4. Bar chart slide
        JSONObject barChartSlide = new JSONObject();
        barChartSlide.put("type", "bar-chart");
        barChartSlide.put("title", "AI Market Growth");
        barChartSlide.put("subtitle", "Global Market Size");

        JSONObject barChartDescription = new JSONObject();
        barChartDescription.put("title", "Market Analysis");
        barChartDescription.put("text", "Global AI market growth projection");

        JSONArray barChartBullets = new JSONArray();
        barChartBullets.put("CAGR of 38%");
        barChartBullets.put("Consistent upward trend");
        barChartDescription.put("bullets", barChartBullets);

        JSONObject chartData = new JSONObject();
        JSONArray chartLabels = new JSONArray();
        chartLabels.put("2020");
        chartLabels.put("2021");
        chartLabels.put("2022");
        chartData.put("labels", chartLabels);

        JSONArray datasets = new JSONArray();
        JSONObject dataset = new JSONObject();
        dataset.put("label", "Market Size ($B)");

        JSONArray dataValues = new JSONArray();
        dataValues.put(50.2);
        dataValues.put(64.1);
        dataValues.put(93.5);
        dataset.put("data", dataValues);
        dataset.put("backgroundColor", "rgba(67, 97, 238, 0.7)");
        dataset.put("borderColor", "rgba(67, 97, 238, 1)");

        datasets.put(dataset);
        chartData.put("datasets", datasets);

        barChartSlide.put("chartData", chartData);
        barChartSlide.put("description", barChartDescription);
        slides.put(barChartSlide);

        // 5. Pie chart slide
        JSONObject pieChartSlide = new JSONObject();
        pieChartSlide.put("type", "pie-chart");
        pieChartSlide.put("title", "Market Share");
        pieChartSlide.put("subtitle", "Industry Breakdown");

        JSONObject pieChartDescription = new JSONObject();
        pieChartDescription.put("title", "Distribution");
        pieChartDescription.put("text", "Industry breakdown of market share");

        JSONArray pieChartBullets = new JSONArray();
        pieChartBullets.put("Company A leads the market");
        pieChartBullets.put("Company B gaining share");
        pieChartDescription.put("bullets", pieChartBullets);

        pieChartSlide.put("insights", "## Market Analysis\n\nCompany A maintains leadership position with 40% market share, while Company B follows closely with 35%.");

        JSONArray metrics = new JSONArray();
        JSONObject metric1 = new JSONObject();
        metric1.put("value", "40%");
        metric1.put("label", "Company A");
        metrics.put(metric1);

        JSONObject metric2 = new JSONObject();
        metric2.put("value", "35%");
        metric2.put("label", "Company B");
        metrics.put(metric2);

        JSONObject metric3 = new JSONObject();
        metric3.put("value", "25%");
        metric3.put("label", "Others");
        metrics.put(metric3);

        pieChartSlide.put("metrics", metrics);

        JSONObject pieChartData = new JSONObject();
        JSONArray pieLabels = new JSONArray();
        pieLabels.put("Company A");
        pieLabels.put("Company B");
        pieLabels.put("Others");
        pieChartData.put("labels", pieLabels);

        JSONArray pieDataValues = new JSONArray();
        pieDataValues.put(40);
        pieDataValues.put(35);
        pieDataValues.put(25);
        pieChartData.put("data", pieDataValues);

        JSONArray backgroundColor = new JSONArray();
        backgroundColor.put("rgba(67, 97, 238, 0.8)");
        backgroundColor.put("rgba(76, 201, 240, 0.8)");
        backgroundColor.put("rgba(247, 37, 133, 0.8)");
        pieChartData.put("backgroundColor", backgroundColor);

        pieChartSlide.put("chartData", pieChartData);
        pieChartSlide.put("description", pieChartDescription);
        slides.put(pieChartSlide);

        // 6. Image slide
        JSONObject imageSlide = new JSONObject();
        imageSlide.put("type", "image");
        imageSlide.put("title", "System Architecture");
        imageSlide.put("subtitle", "Visual Reference");

        JSONObject imageDescription = new JSONObject();
        imageDescription.put("title", "Image Context");
        imageDescription.put("text", "High-level system architecture overview");

        JSONArray imageBullets = new JSONArray();
        imageBullets.put("Microservices architecture");
        imageBullets.put("Cloud-native deployment");
        imageDescription.put("bullets", imageBullets);

        imageSlide.put("imageUrl", "architecture.png");
        imageSlide.put("caption", "System architecture diagram showing key components and their interactions");
        imageSlide.put("description", imageDescription);
        slides.put(imageSlide);

        // 7. Conclusion slide
        JSONObject conclusionSlide = new JSONObject();
        conclusionSlide.put("type", "content");
        conclusionSlide.put("title", "Conclusion");
        conclusionSlide.put("subtitle", "Key Takeaways");

        JSONObject conclusionDescription = new JSONObject();
        conclusionDescription.put("title", "Summary");
        conclusionDescription.put("text", "Thank you for your attention");

        JSONArray conclusionBullets = new JSONArray();
        conclusionBullets.put("For more information, please contact us");
        conclusionBullets.put("Questions and feedback welcome");
        conclusionDescription.put("bullets", conclusionBullets);

        conclusionSlide.put("description", conclusionDescription);
        conclusionSlide.put("content", "## Thank You\n\n* Questions?\n* Comments?\n* Feedback?");
        slides.put(conclusionSlide);

        // Add this to your createComprehensivePresentation method
// 8. Mermaid diagram slide
        JSONObject mermaidSlide = new JSONObject();
        mermaidSlide.put("type", "mermaid");
        mermaidSlide.put("title", "User Flow Diagram");
        mermaidSlide.put("subtitle", "Mobile App Navigation");

        JSONObject mermaidDescription = new JSONObject();
        mermaidDescription.put("title", "Flow Visualization");
        mermaidDescription.put("text", "User journey through the application");

        JSONArray mermaidBullets = new JSONArray();
        mermaidBullets.put("Login flow");
        mermaidBullets.put("Main navigation paths");
        mermaidDescription.put("bullets", mermaidBullets);

// Sample flowchart diagram code
        String diagramCode = "graph LR\n" +
                "    A[Start] --> B{Login?}\n" +
                "    B -->|Yes| C[Dashboard]\n" +
                "    B -->|No| D[Registration]\n" +
                "    D --> B\n" +
                "    C --> E[Profile]\n" +
                "    C --> F[Settings]\n" +
                "    C --> G[Logout]";

        mermaidSlide.put("diagramCode", diagramCode);
        mermaidSlide.put("description", mermaidDescription);
        slides.put(mermaidSlide);

        presentation.put("slides", slides);
        return presentation;
    }
}