package com.github.istin.dmtools.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.istin.dmtools")
public class DmToolsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmToolsServerApplication.class, args);
    }

} 