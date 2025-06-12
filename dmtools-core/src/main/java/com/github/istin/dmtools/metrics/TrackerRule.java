package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;

public interface TrackerRule<T extends ITicket> extends Rule<TrackerClient, T> {

}