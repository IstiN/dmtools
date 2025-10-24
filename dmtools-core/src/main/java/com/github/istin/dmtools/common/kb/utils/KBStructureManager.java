package com.github.istin.dmtools.common.kb.utils;

import com.github.istin.dmtools.common.kb.KBStatistics;
import com.github.istin.dmtools.common.kb.KBStructureBuilder;
import com.github.istin.dmtools.common.kb.model.*;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Encapsulates structure building steps previously handled in KBOrchestrator.
 */
public class KBStructureManager {

    private final KBStructureBuilder structureBuilder;
    private final PersonStatsCollector statsCollector;
    private final KBStatistics statistics;
    private final KBContextLoader contextLoader;

    public KBStructureManager(KBStructureBuilder structureBuilder,
                              PersonStatsCollector statsCollector,
                              KBStatistics statistics,
                              KBContextLoader contextLoader) {
        this.structureBuilder = structureBuilder;
        this.statsCollector = statsCollector;
        this.statistics = statistics;
        this.contextLoader = contextLoader;
    }

    public void buildStructure(AnalysisResult analysisResult,
                               Path outputPath,
                               String sourceName,
                               Map<String, PersonContributions> personContributions,
                               Logger logger) throws Exception {
        // Map temporary IDs (q_1, a_1, n_1) to permanent IDs (q_0001, a_0001, n_0001)
        KBContext idMappingContext = contextLoader.loadKBContext(outputPath);
        KBIdMapper idMapper = new KBIdMapper();
        idMapper.mapAndUpdateIds(analysisResult, idMappingContext);
        
        // Build answers first
        for (Answer answer : analysisResult.getAnswers()) {
            structureBuilder.buildAnswerFile(answer, outputPath, sourceName);
        }

        // Build questions with references
        for (Question question : analysisResult.getQuestions()) {
            structureBuilder.buildQuestionFile(question, outputPath, sourceName, analysisResult);
        }

        // Build notes
        for (Note note : analysisResult.getNotes()) {
            structureBuilder.buildNoteFile(note, outputPath, sourceName);
        }

        // Build topic & area structures
        structureBuilder.buildTopicFiles(analysisResult, outputPath, sourceName);
        structureBuilder.buildAreaStructure(analysisResult, outputPath, sourceName);

        // Track which people have contributions from the current analysis
        Set<String> peopleFromCurrentAnalysis = new HashSet<>();
        if (analysisResult.getQuestions() != null) {
            analysisResult.getQuestions().forEach(q -> {
                if (q.getAuthor() != null) {
                    peopleFromCurrentAnalysis.add(structureBuilder.normalizePersonName(q.getAuthor()));
                }
            });
        }
        if (analysisResult.getAnswers() != null) {
            analysisResult.getAnswers().forEach(a -> {
                if (a.getAuthor() != null) {
                    peopleFromCurrentAnalysis.add(structureBuilder.normalizePersonName(a.getAuthor()));
                }
            });
        }
        if (analysisResult.getNotes() != null) {
            analysisResult.getNotes().forEach(n -> {
                if (n.getAuthor() != null) {
                    peopleFromCurrentAnalysis.add(structureBuilder.normalizePersonName(n.getAuthor()));
                }
            });
        }
        
        if (logger != null && !peopleFromCurrentAnalysis.isEmpty()) {
            logger.debug("People from current analysis ({}): {}", peopleFromCurrentAnalysis.size(), peopleFromCurrentAnalysis);
        }

        Map<String, PersonStatsCollector.PersonStats> personStats;
        Map<String, PersonContributions> contributions;

        if (personContributions != null && !personContributions.isEmpty()) {
            // Reload context after creating files to get updated question/answer/note lists
            KBContext context = contextLoader.loadKBContext(outputPath);
            personStats = new HashMap<>();
            for (String person : context.getExistingPeople()) {
                // Normalize person name to match peopleFromCurrentAnalysis format
                String normalizedPerson = structureBuilder.normalizePersonName(person);
                personStats.putIfAbsent(normalizedPerson, new PersonStatsCollector.PersonStats());
            }
            
            // IMPORTANT: Also add people from current analysis (personContributions)
            // On first run, context.getExistingPeople() is empty, so we need to add people from current analysis
            for (String person : personContributions.keySet()) {
                personStats.putIfAbsent(person, new PersonStatsCollector.PersonStats());
            }

            Map<String, List<KBContext.QuestionSummary>> questionsByPerson = new HashMap<>();
            for (KBContext.QuestionSummary summary : context.getExistingQuestions()) {
                String author = summary.getAuthor();
                if (author != null) {
                    // Normalize author name using the same method as peopleFromCurrentAnalysis
                    String normalizedAuthor = structureBuilder.normalizePersonName(author);
                    questionsByPerson.computeIfAbsent(normalizedAuthor, key -> new ArrayList<>()).add(summary);
                }
            }

            for (Map.Entry<String, PersonStatsCollector.PersonStats> entry : personStats.entrySet()) {
                PersonStatsCollector.PersonStats stats = entry.getValue();
                List<KBContext.QuestionSummary> authoredQuestions = questionsByPerson.get(entry.getKey());
                if (authoredQuestions != null) {
                    stats.questions = (int) authoredQuestions.stream()
                            .map(KBContext.QuestionSummary::getId)
                            .filter(id -> Files.exists(outputPath.resolve("questions").resolve(id + ".md")))
                            .count();
                }
            }

            // CRITICAL: Merge personContributions from current analysis with existing contributions from files
            // Otherwise, contributions from previous sessions will be lost
            Map<String, PersonContributions> existingContributions = statsCollector.collectPersonContributionsFromFiles(outputPath);
            contributions = new HashMap<>(existingContributions);
            
            // Merge new contributions into existing ones
            for (Map.Entry<String, PersonContributions> entry : personContributions.entrySet()) {
                String person = entry.getKey();
                PersonContributions newContribs = entry.getValue();
                
                PersonContributions merged = contributions.computeIfAbsent(person, k -> new PersonContributions());
                
                // Merge questions (avoid duplicates by ID)
                Set<String> existingQuestionIds = merged.getQuestions().stream()
                    .map(PersonContributions.ContributionItem::getId)
                    .collect(java.util.stream.Collectors.toSet());
                for (PersonContributions.ContributionItem q : newContribs.getQuestions()) {
                    if (!existingQuestionIds.contains(q.getId())) {
                        merged.getQuestions().add(q);
                    }
                }
                
                // Merge answers (avoid duplicates by ID)
                Set<String> existingAnswerIds = merged.getAnswers().stream()
                    .map(PersonContributions.ContributionItem::getId)
                    .collect(java.util.stream.Collectors.toSet());
                for (PersonContributions.ContributionItem a : newContribs.getAnswers()) {
                    if (!existingAnswerIds.contains(a.getId())) {
                        merged.getAnswers().add(a);
                    }
                }
                
                // Merge notes (avoid duplicates by ID)
                Set<String> existingNoteIds = merged.getNotes().stream()
                    .map(PersonContributions.ContributionItem::getId)
                    .collect(java.util.stream.Collectors.toSet());
                for (PersonContributions.ContributionItem n : newContribs.getNotes()) {
                    if (!existingNoteIds.contains(n.getId())) {
                        merged.getNotes().add(n);
                    }
                }
                
                // Merge topics (accumulate counts)
                Map<String, Integer> topicCounts = new HashMap<>();
                for (PersonContributions.TopicContribution t : merged.getTopics()) {
                    topicCounts.put(t.getTopicId(), t.getCount());
                }
                for (PersonContributions.TopicContribution t : newContribs.getTopics()) {
                    topicCounts.merge(t.getTopicId(), t.getCount(), Integer::sum);
                }
                merged.getTopics().clear();
                for (Map.Entry<String, Integer> tc : topicCounts.entrySet()) {
                    merged.getTopics().add(new PersonContributions.TopicContribution(tc.getKey(), tc.getValue()));
                }
            }
            
            if (logger != null) {
                logger.debug("Merged contributions: size={}, keys={}", contributions.size(), contributions.keySet());
                for (Map.Entry<String, PersonContributions> entry : contributions.entrySet()) {
                    logger.debug("  - {}: Q={}, A={}, N={}", entry.getKey(),
                        entry.getValue().getQuestions().size(),
                        entry.getValue().getAnswers().size(),
                        entry.getValue().getNotes().size());
                }
            }

            Path answersDir = outputPath.resolve("answers");
            Path notesDir = outputPath.resolve("notes");

            if (Files.exists(answersDir)) {
                try (Stream<Path> files = Files.list(answersDir)) {
                    files.filter(Files::isRegularFile)
                            .forEach(path -> {
                                try {
                                    String content = Files.readString(path);
                                    String author = statsCollector.extractAuthor(content);
                                    if (author != null) {
                                        // Normalize author name using the same method as peopleFromCurrentAnalysis
                                        String normalizedAuthor = structureBuilder.normalizePersonName(author);
                                        PersonStatsCollector.PersonStats stats = personStats.computeIfAbsent(normalizedAuthor, k -> new PersonStatsCollector.PersonStats());
                                        stats.answers++;
                                    }
                                } catch (java.io.IOException ignored) {
                                }
                            });
                }
            }

            if (Files.exists(notesDir)) {
                try (Stream<Path> files = Files.list(notesDir)) {
                    files.filter(Files::isRegularFile)
                            .forEach(path -> {
                                try {
                                    String content = Files.readString(path);
                                    String author = statsCollector.extractAuthor(content);
                                    if (author != null) {
                                        // Normalize author name using the same method as peopleFromCurrentAnalysis
                                        String normalizedAuthor = structureBuilder.normalizePersonName(author);
                                        PersonStatsCollector.PersonStats stats = personStats.computeIfAbsent(normalizedAuthor, k -> new PersonStatsCollector.PersonStats());
                                        stats.notes++;
                                    }
                                } catch (java.io.IOException ignored) {
                                }
                            });
                }
            }
        } else {
            personStats = statsCollector.collectPersonStatsFromFiles(outputPath);
            contributions = statsCollector.collectPersonContributionsFromFiles(outputPath);
        }

        if (logger != null) {
            logger.debug("personStats keys: {}", personStats.keySet());
            logger.debug("contributions keys: {}", contributions.keySet());
        }
        
        for (Map.Entry<String, PersonStatsCollector.PersonStats> entry : personStats.entrySet()) {
            PersonStatsCollector.PersonStats stats = entry.getValue();
            PersonContributions contribution = contributions.get(entry.getKey());
            
            if (logger != null) {
                if (contribution != null) {
                    logger.debug("Person '{}': contribution found - Q={}, A={}, N={}", 
                        entry.getKey(), 
                        contribution.getQuestions().size(), 
                        contribution.getAnswers().size(), 
                        contribution.getNotes().size());
                } else {
                    logger.warn("No contribution found for person '{}' (key in contributions: {})", 
                        entry.getKey(), contributions.containsKey(entry.getKey()));
                    logger.debug("Available contribution keys: {}", contributions.keySet());
                }
            }
            
            // Only pass sourceName if this person has contributions from current analysis
            boolean inCurrentAnalysis = peopleFromCurrentAnalysis.contains(entry.getKey());
            String sourceToAdd = inCurrentAnalysis ? sourceName : null;
            
            if (logger != null) {
                logger.debug("Processing person '{}': inCurrentAnalysis={}, sourceToAdd={}, stats=[Q:{}, A:{}, N:{}], key='{}', peopleSet contains key={}",
                        entry.getKey(),
                        inCurrentAnalysis,
                        sourceToAdd,
                        stats.questions,
                        stats.answers,
                        stats.notes,
                        entry.getKey(),
                        peopleFromCurrentAnalysis.contains(entry.getKey()));
                if (!inCurrentAnalysis && entry.getKey().contains("Tarasevich")) {
                    logger.debug("DEBUG: peopleFromCurrentAnalysis={}", peopleFromCurrentAnalysis);
                    logger.debug("DEBUG: Looking for key='{}' in set", entry.getKey());
                }
            }
            
            structureBuilder.buildPersonProfile(
                    entry.getKey(),
                    outputPath,
                    sourceToAdd,
                    stats.questions,
                    stats.answers,
                    stats.notes,
                    contribution
            );
        }

        if (logger != null) {
            logger.info("Built {} person profiles.", personStats.size());
        }

        updateTopicStatistics(analysisResult, outputPath, logger);
    }

    public void rebuildPeopleProfiles(Path outputPath,
                                      String sourceName,
                                      Logger logger) throws Exception {
        Map<String, PersonStatsCollector.PersonStats> personStats =
                statsCollector.collectPersonStatsFromFiles(outputPath);
        Map<String, PersonContributions> personContributions =
                statsCollector.collectPersonContributionsFromFiles(outputPath);

        for (Map.Entry<String, PersonStatsCollector.PersonStats> entry : personStats.entrySet()) {
            PersonStatsCollector.PersonStats stats = entry.getValue();
            PersonContributions contributions = personContributions.get(entry.getKey());
            structureBuilder.buildPersonProfile(
                    entry.getKey(),
                    outputPath,
                    sourceName,
                    stats.questions,
                    stats.answers,
                    stats.notes,
                    contributions
            );
        }

        if (logger != null) {
            logger.info("Rebuilt {} person profiles.", personStats.size());
        }
    }

    public void generateIndexes(Path outputPath) throws Exception {
        statistics.generateStatistics(outputPath);
        structureBuilder.generatePeopleIndex(outputPath);
    }

    public KBResult buildResult(AnalysisResult analysisResult,
                                Path outputPath,
                                KBFileUtils fileUtils) throws Exception {
        KBResult result = new KBResult();
        result.setSuccess(true);

        if (analysisResult != null) {
            result.setMessage("Knowledge base built successfully");
            result.setQuestionsCount(analysisResult.getQuestions() != null ? analysisResult.getQuestions().size() : 0);
            result.setAnswersCount(analysisResult.getAnswers() != null ? analysisResult.getAnswers().size() : 0);
            result.setNotesCount(analysisResult.getNotes() != null ? analysisResult.getNotes().size() : 0);
        } else {
            result.setMessage("KB processing completed.");
        }

        result.setPeopleCount(fileUtils.countDirectories(outputPath.resolve("people")));
        result.setTopicsCount(fileUtils.countFiles(outputPath.resolve("topics"),
                file -> file.toString().endsWith(".md") && !file.toString().endsWith("-desc.md")));
        result.setAreasCount(fileUtils.countDirectories(outputPath.resolve("areas")));
        result.setQuestionsCount(fileUtils.countFiles(outputPath.resolve("questions"),
                file -> file.getFileName().toString().endsWith(".md")));
        result.setAnswersCount(fileUtils.countFiles(outputPath.resolve("answers"),
                file -> file.getFileName().toString().endsWith(".md")));
        result.setNotesCount(fileUtils.countFiles(outputPath.resolve("notes"),
                file -> file.getFileName().toString().endsWith(".md")));

        return result;
    }

    private void updateTopicStatistics(AnalysisResult analysisResult,
                                       Path outputPath,
                                       Logger logger) throws Exception {
        Map<String, TopicStatistics> topicStats = new HashMap<>();

        for (Question q : analysisResult.getQuestions()) {
            if (q.getTopics() != null) {
                for (String topic : q.getTopics()) {
                    TopicStatistics stats = topicStats.computeIfAbsent(topic, k -> new TopicStatistics());
                    stats.questions++;
                    if (q.getAuthor() != null) {
                        stats.contributors.add(q.getAuthor());
                    }
                }
            }
        }

        for (Answer a : analysisResult.getAnswers()) {
            if (a.getTopics() != null) {
                for (String topic : a.getTopics()) {
                    TopicStatistics stats = topicStats.computeIfAbsent(topic, k -> new TopicStatistics());
                    stats.answers++;
                    if (a.getAuthor() != null) {
                        stats.contributors.add(a.getAuthor());
                    }
                }
            }
        }

        for (Note n : analysisResult.getNotes()) {
            if (n.getTopics() != null) {
                for (String topic : n.getTopics()) {
                    TopicStatistics stats = topicStats.computeIfAbsent(topic, k -> new TopicStatistics());
                    stats.notes++;
                    if (n.getAuthor() != null) {
                        stats.contributors.add(n.getAuthor());
                    }
                }
            }
        }

        for (Map.Entry<String, TopicStatistics> entry : topicStats.entrySet()) {
            TopicStatistics stats = entry.getValue();
            structureBuilder.updateTopicWithStats(
                    outputPath,
                    entry.getKey(),
                    stats.questions,
                    stats.answers,
                    stats.notes,
                    new ArrayList<>(stats.contributors)
            );
        }

        if (logger != null) {
            logger.info("Updated statistics for {} topics", topicStats.size());
        }
    }

    private static class TopicStatistics {
        int questions = 0;
        int answers = 0;
        int notes = 0;
        Set<String> contributors = new HashSet<>();
    }
}
