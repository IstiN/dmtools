package com.github.istin.dmtools.team;

public interface IEmployees {

    String UNKNOWN = "Unknown";

    boolean contains(String fullName);

    String transformName(String sourceFullName);

    boolean isBot(String sourceFullName);
}
