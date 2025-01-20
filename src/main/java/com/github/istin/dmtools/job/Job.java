package com.github.istin.dmtools.job;

import com.github.istin.dmtools.ai.AI;

public interface Job<Params> {

    void runJob(Params params) throws Exception;

    String getName();

    Class<Params> getParamsClass();

    AI getAi();
}
