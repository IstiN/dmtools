package com.github.istin.dmtools.documentation.area;

import com.github.istin.dmtools.common.model.Key;

import java.io.IOException;

public interface KeyAreaMapper {
    String getAreaForTicket(Key key) throws IOException;

    void setAreaForTicket(Key key, String area) throws IOException;
}
