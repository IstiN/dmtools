package com.github.istin.dmtools.team;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Employees implements IEmployees {

    @Override
    public boolean contains(String fullName) {
        init();
        int level = getLevel(fullName);
        return level != -1;
    }

    @Override
    public String transformName(String sourceFullName) {
        return sourceFullName;
    }

    @Override
    public boolean isBot(String sourceFullName) {
        return false;
    }


    private JSONArray employees;

    private JSONObject aliases;

    private static Employees instance;


    private Employees() {

    }

    public static Employees getInstance() {
        if (instance == null) {
            instance = new Employees();
        }
        return instance;
    }

    public void printAllMails() {
        init();
        for (int i = 0; i < employees.length(); i++) {
            JSONObject jsonObject = employees.getJSONObject(i);
            String employee = jsonObject.optString("Employee");
            System.out.print(employee.replace(" ", "_") + "@epam.com;");
        }
    }

    private void init() {
        if (employees == null) {
            readEmployeesJSON();
            readAliasesJSON();
        }
    }

    private void readEmployeesJSON() {
        InputStream input = null;
        try {
            input = getClass().getResourceAsStream("/employees.json");
            if (input != null) {
                String source = convertInputStreamToString(input);
                employees = new JSONArray(source);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Property file not found");
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readAliasesJSON() {
        InputStream input = null;
        try {
            input = getClass().getResourceAsStream("/aliases.json");
            if (input != null) {
                String source = convertInputStreamToString(input);
                aliases = new JSONObject(source);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Property file not found");
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getLevel(String employeeName) {
        init();
        for (int i = 0; i < employees.length(); i++) {
            JSONObject jsonObject = employees.getJSONObject(i);
            String employee = jsonObject.optString("Employee");
            if (employee.equalsIgnoreCase(employeeName) || checkInAliases(employee, employeeName)) {
                String level = jsonObject.optString("Level");
                if (level.startsWith("A")) {
                    return Integer.parseInt(level.substring(1));
                } else if (level.startsWith("B")) {
                    return 4 + Integer.parseInt(level.substring(1));
                } else {
                    return Integer.parseInt(level.substring(1));
                }
            }
        }
        return -1;
    }

    private boolean checkInAliases(String mainName, String queryEmployeeName) {
        JSONArray objects = aliases.optJSONArray(mainName);
        if (objects != null) {
            for (int i = 0; i < objects.length(); i++) {
                String alias = objects.getString(i);
                if (alias.equalsIgnoreCase(queryEmployeeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private static String convertInputStreamToString(InputStream is) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");
    }
}
