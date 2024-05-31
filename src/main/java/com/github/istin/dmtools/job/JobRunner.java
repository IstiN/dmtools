package com.github.istin.dmtools.job;


import com.github.istin.dmtools.estimations.JEstimator;

import java.util.Base64;

public class JobRunner {

    public static void main(String[] args) throws Exception {
        Job job = new Job(new String(decodeBase64(args[0])));
        if (job.getName().equalsIgnoreCase(JEstimator.NAME)) {
            JEstimator.runJob(job.getJEstimatorParams());
        }

    }

    public static java.lang.String decodeBase64(java.lang.String input) {
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        // Convert the decoded bytes to a string
        return new java.lang.String(decodedBytes);
    }
}
