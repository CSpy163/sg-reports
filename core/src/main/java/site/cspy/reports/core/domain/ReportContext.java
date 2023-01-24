package site.cspy.reports.core.domain;

import lombok.Data;
import site.cspy.reports.core.ReportOperator;

import java.util.List;
import java.util.Map;

@Data
public class ReportContext {
    private List<ReportInfo> infos;
    private Map<String, ReportOperator> operatorMap;
}
