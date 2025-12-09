package com.github.istin.dmtools.job;

public class JobRunnerLauncher {

    public static void main(String[] args) throws Exception {
        JobRunner.main(new String[] {"run", "agents/mermaid_diagrams_generator.json"});
    }

}

