package com.tascape.qa.thr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong
 */
@ManagedBean(name = "perfHistory", eager = false)
@RequestScoped
public class PerfHistory extends ResultBase {
    private static final Logger LOG = LoggerFactory.getLogger(PerfHistory.class);

    @ManagedProperty(value = "#{param.suiteids}")
    private String suiteIds;

    private final Map<String, String> suiteIdsDisplay = new HashMap<>();

    private List<Map<String, Object>> perfHistory;

    private List<Map<String, Object>> perfHistoryList;

    private Map<String, Map<String, Object>> suiteMap;

    private CartesianChartModel perfLineChart;

    public void loadPerfHistory() throws SQLException, NamingException {
        String[] ids = this.suiteIds.split(",");
        String[] sids = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sids[i] = "\"" + ids[i] + "\"";
        }
        String[] sidsDiaplay = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sidsDiaplay[i] = StringUtils.right(ids[i], 8);
            this.suiteIdsDisplay.put(ids[i], sidsDiaplay[i]);
        }
        {
            try (Connection conn = this.getConnection()) {
                String sql = "SELECT * "
                        + "FROM "
                        + "th.test_perf tp "
                        + "    INNER JOIN"
                        + "  th.test_result tr ON tp.TEST_RESULT_ID = tr.TEST_RESULT_ID"
                        + "    INNER JOIN"
                        + "  th.test_case tc ON tr.TEST_CASE_ID = tc.TEST_CASE_ID "
                        + "WHERE"
                        + "  tr.TEST_RESULT_ID in (SELECT"
                        + "      TEST_RESULT_ID"
                        + "    FROM"
                        + "       test_result"
                        + "    WHERE"
                        + "       suite_result in ("
                        + StringUtils.join(sids, ",")
                        + ")) ORDER BY tc.SUITE_CLASS, tc.TEST_CLASS, tc.TEST_METHOD, tc.TEST_DATA_INFO, tp.PERF_NAME;";
                LOG.trace(sql);
                Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery(sql);
                this.perfHistoryList = this.dumpResultSetToList(rs);
            }
        }
        this.reorder();
        {
            try (Connection conn = this.getConnection()) {
                String sql = "SELECT SUITE_RESULT_ID, START_TIME FROM suite_result WHERE SUITE_RESULT_ID IN ("
                        + StringUtils.join(sids, ",") + ");";
                LOG.trace(sql);
                Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery(sql);
                this.suiteMap = this.dumpResultSetToMap(rs, "SUITE_RESULT_ID");
            }
        }

        this.perfLineChart = new CartesianChartModel();
        for (Map<String, Object> perfEntry : this.perfHistory) {
            int index = -1;
            LineChartSeries series = new LineChartSeries();
            series.setLabel(perfEntry.get("PERF_NAME") + "");
            for (String sid : ids) {
                index++;
                int data = -1;
                try {
                    data = Integer.parseInt(perfEntry.get(sid) + "");
                } catch (NumberFormatException ex) {
                    LOG.warn("wx {}", ex.getMessage());
                }
                if (data <= 0) {
                    series.set(index + "", null);
                } else {
                    series.set(index + "", data);
                }
            }
            this.perfLineChart.addSeries(series);
        }
    }

    public Map<String, String> getSuiteIdsDisplay() {
        return suiteIdsDisplay;
    }

    public CartesianChartModel getPerfLines() {
        return perfLineChart;
    }

    private void reorder() {
        this.perfHistory = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> perf : this.perfHistoryList) {
            String sid = perf.get("SUITE_RESULT") + "";
            String name = perf.get("PERF_NAME") + "";
            String suite = perf.get("SUITE_CLASS") + "";
            String clazz = perf.get("TEST_CLASS") + "";
            String method = perf.get("TEST_METHOD") + "";
            String dataInfo = perf.get("TEST_DATA_INFO") + "";
            String data = perf.get("TEST_DATA") + "";
            String key = name + suite + clazz + method + dataInfo + data;

            boolean toAdd = true;
            for (Map<String, Object> perfEntry : this.perfHistory) {
                if (perfEntry.get(key) != null) {
                    perfEntry.put(sid, perf.get("PERF_DATA"));
                    perfEntry.put(sid + "_log_url",
                            this.getLogUrl(perf.get("TEST_STATION") + "", perf.get("LOG_DIR") + ""));
                    toAdd = false;
                    break;
                }
            }
            if (toAdd) {
                perf.put(key, true);
                perf.put("_row_index", index);
                perf.put("_suite_class", StringUtils.substringAfterLast(perf.get("SUITE_CLASS") + "", "."));
                perf.put(sid, perf.get("PERF_DATA"));
                perf.put(sid + "_log_url",
                        this.getLogUrl(perf.get("TEST_STATION") + "", perf.get("LOG_DIR") + ""));
                this.perfHistory.add(perf);
                index++;
            }
        }
    }

    public String getSuiteIds() {
        return suiteIds;
    }

    public void setSuiteIds(String suiteIds) {
        this.suiteIds = suiteIds;
    }

    public List<Map<String, Object>> getPerfHistory() {
        return perfHistory;
    }

    public void setPerfHistory(List<Map<String, Object>> perfHistory) {
        this.perfHistory = perfHistory;
    }

    public Map<String, Map<String, Object>> getSuiteMap() {
        return suiteMap;
    }

    public void setSuiteMap(Map<String, Map<String, Object>> suiteMap) {
        this.suiteMap = suiteMap;
    }
}
