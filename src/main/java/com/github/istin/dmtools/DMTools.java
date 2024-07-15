package com.github.istin.dmtools;

import com.github.istin.dmtools.report.timeinstatus.TimeInStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DMTools {

    private static final Logger logger = LogManager.getLogger(DMTools.class);

    public static void main(String[] args) {
        System.out.println("Hello Dear DM.");
        System.out.println("Root logger level: " + LogManager.getRootLogger().getLevel());
        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warn("This is a warn message");
        logger.error("This is an error message");
    }

}
