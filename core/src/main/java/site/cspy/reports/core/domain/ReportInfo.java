package site.cspy.reports.core.domain;


import lombok.Data;

/**
 * 报表描述类，每个报表模块都需要提供本类实例。
 */
@Data
public class ReportInfo {
    public ReportInfo() {
    }

    /**
     * 报表名称
     */
    private String name;

    /**
     * 报表编号
     */
    private String code;

    /**
     * 报表缩略图
     */
    private String thumbnailBase64;

    /**
     * 报表描述
     */
    private String description;

    /**
     * 报表版本
     */
    private String version;

}
