package com.github.istin.dmtools.report;

import com.github.istin.dmtools.metrics.Metric;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.Employees;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevChart {
    private static final Logger logger = LogManager.getLogger(DevChart.class);
    private String devName;

    private List<String> headers = new ArrayList<>();

    public List<ReportIterationData> reportIterationDataList = new ArrayList<ReportIterationData>();

    public DevChart(String dev) {
        devName = dev;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public static class ReportIterationData {
        private String reportIterationName;
        private int reportIterationId;
        private String formula;

        public Map<Metric, List<KeyTime>> customMetrics = new HashMap<>();
        private int defaultCurrentIteration;

        public List<Metric> getCustomMetricsHeaders() {
            return customMetricsHeaders;
        }

        public List<Metric> customMetricsHeaders;

        public String getReportIterationName() {
            return reportIterationName;
        }

        public int getReportIterationId() {
            return reportIterationId;
        }

        public ReportIterationData(int reportIterationId, String reportIterationName, int defaultCurrentIteration, String formula) {
            this.reportIterationName = reportIterationName;
            this.reportIterationId = reportIterationId;
            this.defaultCurrentIteration = defaultCurrentIteration;
            this.formula = formula;
        }

        public int getScoreAsInteger() throws ScriptException {
            String score = getScore();
            if (score.equalsIgnoreCase("NaN")) {
                return 0;
            }
            return Integer.parseInt(score);
        }

        public String getScore() throws ScriptException {
            ScriptEngineManager mgr = new ScriptEngineManager();
            javax.script.ScriptEngine engine = mgr.getEngineByName("graal.js");
            //
            if (formula.endsWith(".js")) {
                HashMap<String, String> params = new HashMap<>();
                for (int i = 0; i < getCustomMetricsHeaders().size(); i++) {
                    Metric metric = getCustomMetricsHeaders().get(i);
                    params.put(metric.getName(), getCustomMetrics().get(i));
                }
                try {
                    formula = readFormula(params, formula);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (TemplateException e) {
                    throw new RuntimeException(e);
                }
            } else {
                for (int i = 0; i < getCustomMetricsHeaders().size(); i++) {
                    Metric metric = getCustomMetricsHeaders().get(i);
                    formula = formula.replaceAll("\\$\\{" + metric.getName() + "}", ""+getCustomMetrics().get(i));
                }
            }

            try {
                if (reportIterationId > defaultCurrentIteration) {
                    return "NaN";
                }
                Object eval = engine.eval(formula);
                if (eval instanceof Integer) {
                    if ((Integer)eval < 0) {
                        eval = 0;
                    }
                    return new DecimalFormat("#.##").format(eval);
                }
                if ((Double)eval < 0) {
                    eval = 0;
                }
                return new DecimalFormat("#.##").format(((Double)eval));
            } catch (Exception e) {
                logger.error(formula);
                logger.error(e);
                return "NaN";
            }
        }

        private String readFormula(HashMap<String, String> params, String template) throws IOException, TemplateException {
            Configurator.initialize(new DefaultConfiguration());

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
            cfg.setLocalizedLookup(false);
            cfg.setTemplateLoader(new ClassTemplateLoader(getClass().getClassLoader(), "/formula"));


            Template temp = cfg.getTemplate( template);


            Writer out = new StringWriter();
            temp.process(params, out);
            return out.toString();
        }

        public String getIterationName() {
            return reportIterationName;
        }

        private List<String> iterationValues;

        public void setIterationValues(List<String> iterationValues) {
            this.iterationValues = iterationValues;
        }

        public List<String> getIterationValues() {
            return iterationValues;
        }

        public List<String> getCustomMetrics() {
            List<String> values = new ArrayList<>();
            for (Metric m : customMetricsHeaders) {
                List<KeyTime> keyTimes = customMetrics.get(m);
                if (keyTimes == null) {
                    values.add("0");
                } else {
                    if (m.isWeight()) {
                        double weight = 0;
                        for (KeyTime keyTime : keyTimes) {
                            weight+= keyTime.getWeight();
                        }
                        values.add(new DecimalFormat("#.##").format(weight));
                    } else {
                        values.add(new DecimalFormat("#.##").format((double)keyTimes.size()));
                    }
                }
            }
            return values;
        }

        public String getFormula() {
            return formula;
        }

        public int getDefaultCurrentIteration() {
            return defaultCurrentIteration;
        }
    }

    public String getId() {
        return devName.replaceAll(" ", "_");
    }


    public List<ReportIterationData> getReportIterationDataList() {
        return reportIterationDataList;
    }

    public String getDevName() {
        return MockedNames.getInstance().mock(devName);
    }

    public String getLevel() {
        int level = Employees.findLevelInAllInstances(devName);
        if (level == 0) {
            return "";
        }
        return ""+level;
    }

    public String getAvgScore() throws ScriptException {
        List<ReportIterationData> reportIterationDataList = getReportIterationDataList();
        int iterCount = 0;
        double avgScore = 0;
        for (int i = reportIterationDataList.size()-1; i >= 0; i--) {
            if (iterCount >= 12) {
                break;
            }
            ReportIterationData reportIterationData = reportIterationDataList.get(i);
            int defaultCurrentIteration = reportIterationData.getDefaultCurrentIteration();
            try {
                int reportIterationNameIntvalue = reportIterationData.getReportIterationId();
                if (reportIterationNameIntvalue < defaultCurrentIteration) {
                    double result = Double.parseDouble(reportIterationData.getScore());
                    if (!Double.isNaN(result)) {
                        avgScore = avgScore + result;
                    }
                    iterCount++;
                }
            } catch (NumberFormatException e) {}
        }
        if (iterCount == 0) {
            return new DecimalFormat("#.##").format(0d);
        }
        return new DecimalFormat("#.##").format((double)(avgScore/iterCount));
    }
}
