package com.github.istin.dmtools.csv;

import com.github.istin.dmtools.metrics.source.CommonSourceCollector;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a CSV file and produces KeyTime entries for reporting metrics.
 * Similar to ExcelMetricSource but for CSV files.
 *
 * Supports:
 * - Configurable columns: whoColumn (person), whenColumn (date), weightColumn (numeric value)
 * - defaultWho: fallback person name when no whoColumn is specified
 * - Quoted numeric values like "79997" or "0.04"
 * - ISO 8601 dates (2026-02-07T13:29:37.369Z) and simple dates (2026-02-07)
 * - Graceful handling of invalid/non-numeric values (skipped)
 * - File path: absolute or relative to working directory
 */
public class CsvMetricSource extends CommonSourceCollector {

    private static final Logger logger = LogManager.getLogger(CsvMetricSource.class);

    private static final Pattern CSV_SPLIT = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private final String filePath;
    private final String whoColumn;
    private final String whenColumn;
    private final String weightColumn;
    private final double weightMultiplier;
    private final String defaultWho;

    public CsvMetricSource(IEmployees employees, String filePath, String whoColumn, String whenColumn,
                           String weightColumn, double weightMultiplier, String defaultWho) {
        super(employees);
        this.filePath = filePath;
        this.whoColumn = whoColumn;
        this.whenColumn = whenColumn;
        this.weightColumn = weightColumn;
        this.weightMultiplier = weightMultiplier;
        this.defaultWho = defaultWho;
    }

    @Override
    public List<KeyTime> performSourceCollection(boolean isPersonalized, String metricName) throws Exception {
        List<KeyTime> keyTimes = new ArrayList<>();

        try (BufferedReader reader = openReader()) {
            if (reader == null) {
                logger.warn("CSV file not found: {}", filePath);
                return keyTimes;
            }

            // Parse header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return keyTimes;
            }
            String[] headers = parseCsvLine(headerLine);
            int whoIdx = findColumnIndex(headers, whoColumn);
            int whenIdx = findColumnIndex(headers, whenColumn);
            int weightIdx = findColumnIndex(headers, weightColumn);

            if (whenIdx == -1) {
                logger.warn("Date column '{}' not found in CSV headers: {}", whenColumn, Arrays.toString(headers));
                return keyTimes;
            }
            if (weightIdx == -1) {
                logger.warn("Weight column '{}' not found in CSV headers: {}", weightColumn, Arrays.toString(headers));
                return keyTimes;
            }

            // Parse rows
            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] values = parseCsvLine(line);

                // Parse date
                Calendar cal = parseDate(safeGet(values, whenIdx));
                if (cal == null) {
                    rowIndex++;
                    continue;
                }

                // Parse weight
                Double weight = parseNumber(safeGet(values, weightIdx));
                if (weight == null) {
                    rowIndex++;
                    continue;
                }

                // Determine who
                String who;
                if (isPersonalized) {
                    if (whoIdx >= 0) {
                        who = safeGet(values, whoIdx).trim();
                        if (getEmployees() != null) {
                            who = getEmployees().transformName(who);
                            if (!getEmployees().contains(who)) {
                                who = IEmployees.UNKNOWN;
                            }
                        }
                    } else if (defaultWho != null && !defaultWho.isEmpty()) {
                        who = defaultWho;
                    } else {
                        who = metricName;
                    }
                } else {
                    who = metricName;
                }

                KeyTime keyTime = new KeyTime(filePath + "_" + rowIndex, cal, who);
                keyTime.setWeight(weight * weightMultiplier);

                // Build summary from available columns
                StringBuilder summary = new StringBuilder();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    if (i == whenIdx || i == weightIdx || i == whoIdx) continue;
                    String v = unquote(values[i]).trim();
                    if (!v.isEmpty()) {
                        if (summary.length() > 0) summary.append(" | ");
                        summary.append(unquote(headers[i]).trim()).append(": ").append(v);
                    }
                }
                if (summary.length() > 0) {
                    keyTime.setSummary(summary.toString());
                }

                keyTimes.add(keyTime);
                rowIndex++;
            }
        }

        logger.info("CSV '{}': collected {} entries from column '{}'", filePath, keyTimes.size(), weightColumn);
        return keyTimes;
    }

    BufferedReader openReader() throws IOException {
        // Try absolute path first, then relative
        File file = new File(filePath);
        if (file.exists()) {
            return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        }
        // Try classpath (for tests)
        InputStream is = getClass().getResourceAsStream(filePath);
        if (is != null) {
            return new BufferedReader(new InputStreamReader(is, "UTF-8"));
        }
        return null;
    }

    static String[] parseCsvLine(String line) {
        String[] parts = CSV_SPLIT.split(line, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unquote(parts[i]);
        }
        return parts;
    }

    static String unquote(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }

    static int findColumnIndex(String[] headers, String columnName) {
        if (columnName == null || columnName.isEmpty()) return -1;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    static Double parseNumber(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        value = value.trim().replace(",", "");
        // Skip known non-numeric strings
        if ("NaN".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)
                || "N/A".equalsIgnoreCase(value) || "-".equals(value) || "inf".equalsIgnoreCase(value)
                || "infinity".equalsIgnoreCase(value) || "+infinity".equalsIgnoreCase(value)
                || "-infinity".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            double d = Double.parseDouble(value);
            if (Double.isNaN(d) || Double.isInfinite(d)) return null;
            return d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
        new SimpleDateFormat("yyyy-MM-dd"),
        new SimpleDateFormat("MM/dd/yyyy"),
        new SimpleDateFormat("dd/MM/yyyy"),
    };

    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        for (SimpleDateFormat fmt : DATE_FORMATS) {
            fmt.setTimeZone(utc);
            fmt.setLenient(false);
        }
    }

    static Calendar parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        value = value.trim();
        for (SimpleDateFormat fmt : DATE_FORMATS) {
            try {
                synchronized (fmt) {
                    Date date = fmt.parse(value);
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTime(date);
                    return cal;
                }
            } catch (Exception ignored) {
            }
        }
        logger.debug("Failed to parse date: {}", value);
        return null;
    }

    private static String safeGet(String[] arr, int idx) {
        if (idx < 0 || idx >= arr.length) return "";
        return arr[idx];
    }
}
