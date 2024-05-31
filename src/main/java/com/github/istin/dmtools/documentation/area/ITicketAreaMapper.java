package com.github.istin.dmtools.documentation.area;

import com.github.istin.dmtools.common.model.ITicket;

import java.io.IOException;

public interface ITicketAreaMapper {
    String getAreaForTicket(ITicket ticket) throws IOException;

    void setAreaForTicket(ITicket ticket, String area) throws IOException;
}
