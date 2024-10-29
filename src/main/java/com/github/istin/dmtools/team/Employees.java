package com.github.istin.dmtools.team;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Employees implements IEmployees {

    public static final String ROLE_TESTER = "Tester";
    public static final String ROLE_DEVELOPER = "Developer";
    public static final String ROLE_BUSINESS_ANALYST = "Business Analyst";
    private final String file;

    private Set<String> unknownNames = new HashSet<>();

    public static int findLevelInAllInstances(String devName) {
        int level = getInstance().getLevel(devName);
        if (level <= 0) {
            Set<String> keys = roleInstances.keySet();
            for (String key : keys) {
                level = roleInstances.get(key).getLevel(devName);
                if (level > 0) {
                    return level;
                }
            }

        }
        return 0;
    }

    @Override
    public boolean contains(String fullName) {
        init();
        int level = getLevel(fullName);
        boolean isContaining = level != -1;
        if (!isContaining) {
            unknownNames.add(fullName);
        }
        return isContaining;
    }

    public Set<String> getUnknownNames() {
        return unknownNames;
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


    private Employees(String file, String filterRole) {
        this.file = file;
        this.filterRole = filterRole;
    }

    public static Employees getInstance() {
        if (instance == null) {
            instance = new Employees(null, null);
        }
        return instance;
    }

    public static Employees getDevelopers(String file) {
        return getInstance(file, ROLE_DEVELOPER);
    }

    public static Employees getDevelopers() {
        return getDevelopers(null);
    }

    public static Employees getBusinessAnalysts(String file) {
        return getInstance(file, ROLE_BUSINESS_ANALYST);
    }

    public static Employees getBusinessAnalysts() {
        return getBusinessAnalysts(null);
    }

    public static Employees getTesters(String file) {
        return getInstance(file, ROLE_TESTER);
    }

    public static Employees getTesters() {
        return getInstance(null, ROLE_TESTER);
    }

    public static Employees getInstance(String file, String role) {
        Employees employees = roleInstances.get(role + file);
        if (employees == null) {
            employees = new Employees(file, role);
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
            if (file == null) {
                input = getClass().getResourceAsStream("/employees.json");
            } else {
                input = getClass().getResourceAsStream("/" + file);
            }
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
            if (file == null) {
                input = getClass().getResourceAsStream("/aliases.json");
            } else {
                input = getClass().getResourceAsStream("/"+ file.split("\\.")[0]+"_aliases.json");
            }
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
