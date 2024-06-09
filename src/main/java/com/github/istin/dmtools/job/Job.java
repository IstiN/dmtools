package com.github.istin.dmtools.job;

public interface Job<Params> {

    void runJob(Params params) throws Exception;

    String getName();

    Class<Params> getParamsClass();

}
