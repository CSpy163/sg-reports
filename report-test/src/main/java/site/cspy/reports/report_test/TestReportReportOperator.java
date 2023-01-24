package site.cspy.reports.report_test;

import org.springframework.stereotype.Component;
import site.cspy.reports.core.ReportOperator;

import java.util.HashMap;
import java.util.Map;

@Component(value = "report-test")
public class TestReportReportOperator implements ReportOperator {

    @Override
    public Map<String, Object> prepareParams(String... params) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", "实骨");
        return variables;
    }

    @Override
    public String reportFileName() {
        return "lshort-zh-cn.pdf";
    }

    @Override
    public String[][] getCommands() {
        return new String[][]{
                {"xelatex", "lshort-zh-cn"},
                {"makeindex", "-s", "lshort-zh-cn.ist", "lshort-zh-cn"},
                {"xelatex", "lshort-zh-cn"},
                {"xelatex", "lshort-zh-cn"}
        };
    }
}
