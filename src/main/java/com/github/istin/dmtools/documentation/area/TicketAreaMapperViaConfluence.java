package com.github.istin.dmtools.documentation.area;

import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.utils.HtmlCleaner;
import org.json.JSONObject;

import java.io.IOException;

public class TicketAreaMapperViaConfluence implements ITicketAreaMapper {

    public static final String TICKET_TO_AREA_MAPPING = " Ticket To Area Mapping";
    private final String areaPrefix;
    private final String rootPageName;
    private BasicConfluence confluence;
    private JSONObject ticketsToAreasMapping;
    private String ticketsToAreaMappingTitle;
    private Content ticketsMappingContent;
    private Content rootContent;

    public TicketAreaMapperViaConfluence(String areaPrefix, String rootPageName, BasicConfluence confluence) {
        this.areaPrefix = areaPrefix;
        this.rootPageName = rootPageName;
        this.confluence = confluence;
    }

    @Override
    public String getAreaForTicket(ITicket ticket) throws IOException {
        if (ticketsToAreasMapping == null) {
            init();
        }
        return ticketsToAreasMapping.optString(ticket.getKey());
    }

    private void init() throws IOException {
        rootContent = confluence.findContent(rootPageName);
        ticketsToAreaMappingTitle = areaPrefix + TICKET_TO_AREA_MAPPING;
        ticketsMappingContent = confluence.findOrCreate(ticketsToAreaMappingTitle, rootContent.getId(), "{}");
        String ticketToAreaMatchingSource = HtmlCleaner.cleanAllHtmlTags("", ticketsMappingContent.getStorage().getValue().replaceAll("&quot;", "\""));
        ticketsToAreasMapping = new JSONObject(ticketToAreaMatchingSource);
    }

    @Override
    public void setAreaForTicket(ITicket ticket, String area) throws IOException {
        ticketsToAreasMapping.put(ticket.getTicketKey(), area);
        confluence.updatePage(ticketsMappingContent.getId(), ticketsToAreaMappingTitle, rootContent.getId(), ticketsToAreasMapping.toString());
    }

}
