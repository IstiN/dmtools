package com.github.istin.dmtools.common.model;

import java.util.List;

public interface ITextMatch {

    String getFragment();

    String getObjectUrl();

    String getObjectType();

    List<IMatch> getMatches();

}