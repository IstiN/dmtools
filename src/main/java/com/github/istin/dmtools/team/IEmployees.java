package com.github.istin.dmtools.team;

public interface IEmployees {
    boolean contains(String fullName);

    String transformName(String sourceFullName);

    boolean isBot(String sourceFullName);
}
