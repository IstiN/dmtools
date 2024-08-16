package com.github.istin.dmtools.team;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Employees implements IEmployees {

    @Override
    public boolean contains(String fullName) {
        init();
        int level = getLevel(fullName);
        return level != -1;
    }

    @Override
    public String transformName(String sourceFullName) {
        return convertNameIfAlias(sourceFullName);
    }

    @Override
    public boolean isBot(String sourceFullName) {
        return sourceFullName.equalsIgnoreCase("DM_scripts") || sourceFullName.equalsIgnoreCase("DM_scripts_token");
    }


    private JSONArray employees;

    private JSONObject aliases;

    private String filterRole;

    private static Employees instance;

    private static Map<String, Employees> roleInstances = new HashMap<>();


    private Employees(String filterRole) {
        this.filterRole = filterRole;
    }

    public static Employees getInstance() {
        if (instance == null) {
            instance = new Employees(null);
        }
        return instance;
    }

    public static Employees getDevelopers() {
        return getInstance("Developer");
    }

    public static Employees getInstance(String role) {
        Employees employees = roleInstances.get(role);
        if (employees == null) {
            employees = new Employees(role);
            roleInstances.put(role, employees);
        }
        return employees;
    }

    public void printAllMails(String emailDomain) {
        init();
        for (int i = 0; i < employees.length(); i++) {
            JSONObject jsonObject = employees.getJSONObject(i);
            String employee = jsonObject.optString("Employee");
            System.out.print(employee.replace(" ", "_") + "@" + emailDomain + ";");
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
                JSONArray sourceEmployees = new JSONArray(source);
                if (filterRole != null) {
                    employees = new JSONArray();
                    for (int i = 0; i < sourceEmployees.length(); i++) {
                        JSONObject employee = sourceEmployees.getJSONObject(i);
                        if (employee.getString("Role").equalsIgnoreCase(filterRole)) {
                            employees.put(employee);
                        }
                    }
                } else {
                    employees = sourceEmployees;
                }
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
        if (this.employees == null) {
            return 0;
        }
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
        if (aliases == null) {
            return false;
        }
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

    private String convertNameIfAlias(String queryEmployeeName) {
        if (aliases == null) {
            return queryEmployeeName;
        }
        Iterator<String> keys = aliases.keys();
        for (Iterator<String> it = keys; it.hasNext(); ) {
            String mainName = it.next();
            JSONArray objects = aliases.optJSONArray(mainName);
            if (objects != null) {
                for (int i = 0; i < objects.length(); i++) {
                    String alias = objects.getString(i);
                    if (alias.equalsIgnoreCase(queryEmployeeName)) {
                        return mainName;
                    }
                }
            }
        }
        return queryEmployeeName;
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
