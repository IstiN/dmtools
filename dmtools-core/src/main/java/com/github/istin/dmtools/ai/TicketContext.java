package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.prompt.input.TicketBasedPrompt;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TicketContext implements ToText {

    private static final Logger logger = LogManager.getLogger(TicketContext.class);

    @Getter
    private List<IComment> comments;

    public static interface OnTicketDetailsRequest {
        ITicket getTicketDetails(String key) throws IOException;
    }

    private final TrackerClient<? extends ITicket> trackerClient;
    @Getter
    private final ITicket ticket;
    @Setter
    @Getter
    private OnTicketDetailsRequest onTicketDetailsRequest;

    @Setter
    @Getter
    private List<ITicket> extraTickets = new ArrayList<>();

    public TicketContext(TrackerClient<? extends ITicket> trackerClient, ITicket ticket) {
        this.trackerClient = trackerClient;
        this.ticket = ticket;
    }

    public void prepareContext() throws IOException {
        prepareContext(false);
    }
    public void prepareContext(boolean withComments) throws IOException {
        long prepareStart = System.currentTimeMillis();
        logger.info("TIMING: Starting TicketContext.prepareContext() for {} at {}", ticket.getKey(), prepareStart);
        
        // Step 1: Extract JIRA IDs from ticket text
        long extractStart = System.currentTimeMillis();
        logger.info("TIMING: Starting ticket.toText() for JIRA ID extraction for {} at {}", ticket.getKey(), extractStart);
        String ticketText = ticket.toText();
        long extractTextDuration = System.currentTimeMillis() - extractStart;
        logger.info("TIMING: ticket.toText() for JIRA ID extraction took {}ms for {} (text length: {})", extractTextDuration, ticket.getKey(), ticketText.length());
        
        long parseStart = System.currentTimeMillis();
        logger.info("TIMING: Starting IssuesIDsParser.extractAllJiraIDs() for {} at {}", ticket.getKey(), parseStart);
        Set<String> keys = IssuesIDsParser.extractAllJiraIDs(ticketText);
        long parseDuration = System.currentTimeMillis() - parseStart;
        logger.info("TIMING: IssuesIDsParser.extractAllJiraIDs() took {}ms for {} and found {} keys: {}", parseDuration, ticket.getKey(), keys.size(), keys);
        
        // Step 2: Fetch extra tickets
        extraTickets = new ArrayList<>();
        if (!keys.isEmpty()) {
            long fetchExtraStart = System.currentTimeMillis();
            logger.info("TIMING: Starting extra tickets fetch for {} at {}", ticket.getKey(), fetchExtraStart);
            for (String key : keys) {
                if (key.equalsIgnoreCase(ticket.getKey())) {
                    continue;
                }

                try {
                    long singleTicketStart = System.currentTimeMillis();
                    ITicket e = null;
                    if (onTicketDetailsRequest != null) {
                        e = onTicketDetailsRequest.getTicketDetails(key);
                    }
                    if (e == null) {
                        logger.info("TIMING: Fetching extra ticket {} via performTicket()", key);
                        e = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
                    }
                    extraTickets.add(e);
                    long singleTicketDuration = System.currentTimeMillis() - singleTicketStart;
                    logger.info("TIMING: Fetching extra ticket {} took {}ms", key, singleTicketDuration);
                } catch (AtlassianRestClient.RestClientException e) {
                    logger.info("TIMING: Failed to fetch extra ticket {}: {}", key, e.getMessage());
                }
            }
            long fetchExtraDuration = System.currentTimeMillis() - fetchExtraStart;
            logger.info("TIMING: All extra tickets fetch took {}ms for {}", fetchExtraDuration, ticket.getKey());
        }
        
        // Step 3: Fetch comments if requested
        if (withComments) {
            long commentsStart = System.currentTimeMillis();
            logger.info("TIMING: Starting comments fetch for {} at {}", ticket.getKey(), commentsStart);
            @SuppressWarnings("unchecked")
            List<IComment> fetchedComments = (List<IComment>) trackerClient.getComments(ticket.getKey(), ticket);
            comments = fetchedComments;
            long commentsDuration = System.currentTimeMillis() - commentsStart;
            logger.info("TIMING: Comments fetch took {}ms for {} ({} comments)", commentsDuration, ticket.getKey(), (comments != null ? comments.size() : 0));
        }
        
        long prepareDuration = System.currentTimeMillis() - prepareStart;
        logger.info("TIMING: Overall TicketContext.prepareContext() took {}ms for {}", prepareDuration, ticket.getKey());
    }

    @Override
    public String toText() throws IOException {
        StringBuilder text = new StringBuilder(new TicketBasedPrompt.TicketWrapper(trackerClient.getBasePath(), ticket).toText());
        if (comments != null) {
            text.append("<previous_discussion>").append("\n");
            for (IComment comment : comments) {
                text.append("\n").append(comment);
            }
            text.append("</previous_discussion>").append("\n");
        }
        for (ITicket extraTicket : extraTickets) {
            text.append("\n").append(new TicketBasedPrompt.TicketWrapper(trackerClient.getBasePath(), extraTicket).toText());
        }
        return text.toString();
    }

}
