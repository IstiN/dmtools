package com.github.istin.dmtools.ai.agent;

public interface IAgent<Params, Result> {

    Result run(Params params) throws Exception;

}
