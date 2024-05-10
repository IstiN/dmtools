package com.github.istin.dmtools.openai;

import com.github.istin.dmtools.openai.input.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import java.io.*;

public class PromptManager {

    public String checkPotentiallyEffectedFilesForTicket(CodeGeneration input) throws IOException, TemplateException {
        return stringFromTemplate(input, "developer_check_potentially_effected_files");
    }

    public String checkTaskTechnicalOrProduct(TicketBasedPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "po_check_story_technical_or_product");
    }

    public String checkStoryAreas(BAStoryAreaPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "ba_recognize_story_areas");
    }

    public String buildNiceLookingDocumentation(TicketBasedPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "ba_nice_looking_documentation");
    }

    public String checkSimilarStories(BASimilarStoriesPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "ba_find_similar_stories");
    }

    public String estimateStory(BASimilarStoriesPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "developer_estimate_story");
    }

    public String validateSimilarStory(BASimilarStoriesPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "developer_validate_similar_story");
    }

    public String requestGenerateCodeForTicket(CodeGeneration input) throws IOException, TemplateException {
        return stringFromTemplate(input, "developer_generate_code");
    }

    public String convertToHTML(InputPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "convert_to_html");
    }

    public String requestTestCasesForStory(TicketBasedPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "qa_story_test_cases_generation");
    }

    public String requestNiceLookingStoryInGherkinStyleAndPotentialQuestionsToPO(TicketBasedPrompt input) throws IOException, TemplateException {
        return stringFromTemplate(input, "ba_nice_looking_story_in_gherkin_style_and_potential_questions_to_po");
    }

    public String requestDeveloperStoryPullRequestReview(PullRequestReview input) throws IOException, TemplateException {
        return stringFromTemplate(input, "developer_story_pull_request_review");
    }

    public String requestDeveloperBugPullRequestReview(PullRequestReview input) throws IOException, TemplateException {
        return stringFromTemplate(input, "developer_bug_pull_request_review");
    }

    public String validatePotentiallyEffectedFile(TicketFilePrompt ticketFilePrompt) throws TemplateException, IOException {
        return stringFromTemplate(ticketFilePrompt, "developer_validate_potentially_effected_file");
    }

    private String stringFromTemplate(Object input, String template) throws IOException, TemplateException {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setLocalizedLookup(false);
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass().getClassLoader(), "/ftl"));


        Template temp = cfg.getTemplate("prompts/" + template + ".md");


        Writer out = new StringWriter();
        temp.process(input, out);
        return out.toString();
    }


}
