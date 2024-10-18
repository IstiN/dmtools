package com.github.istin.dmtools.diagram;

import com.github.istin.dmtools.ai.Diagram;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;

public class DiagramsDrawer {

    private static final Logger logger = LogManager.getLogger(DiagramsDrawer.class);

    private final String folder;

    public DiagramsDrawer() {
        this.folder = "cache" + getClass().getSimpleName();
    }


    public File draw(String name, Diagram diagram) throws Exception {
        File cachedFile = new File(folder + "/" + name.replaceAll(" ", "_") + ".html");
        FileUtils.writeStringToFile(cachedFile, stringFromTemplate(diagram));
        return makeScreenshot(folder, cachedFile);
    }

    public static File makeScreenshot(String cacheFolder, File file) throws Exception {
        // Set the path to the chromedriver executable
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        // Create an instance of ChromeDriver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
//        WebDriver driver = new ChromeDriver();
        https://stackoverflow.com/questions/16180428/can-selenium-webdriver-open-browser-windows-silently-in-the-background
        try {
            // Navigate to the desired URL
            //driver.get("file:///path/to/your/HTMLfile.html");
            driver.get("file://"+ file.getAbsolutePath());

            Thread.sleep(5000);

            // Locate the div with the class "mermaid"
            WebElement element = driver.findElement(By.className("mermaid"));

            // Create a WebDriverWait instance with a timeout of 10 seconds
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOf(element));

            // Take a screenshot of the element
            File screenshot = element.getScreenshotAs(OutputType.FILE);
            File destinationFile = new File(cacheFolder, file.getName().replaceAll(".html", "") + "_screenshot.png");

            // Copy the screenshot to the destination file
            FileHandler.copy(screenshot, destinationFile);

            logger.info("Screenshot saved to {}", destinationFile.getAbsolutePath());
            return destinationFile;
        } finally {
            // Close the browser
            driver.quit();
        }
    }

    private String stringFromTemplate(Object input) throws IOException, TemplateException {
        Configurator.initialize(new DefaultConfiguration());

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setLocalizedLookup(false);
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass().getClassLoader(), "/ftl"));


        Template temp = cfg.getTemplate("diagrams/index.html");


        Writer out = new StringWriter();
        temp.process(input, out);
        return out.toString();
    }
}
