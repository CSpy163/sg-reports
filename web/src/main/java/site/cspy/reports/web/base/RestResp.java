package site.cspy.reports.web.base;

import lombok.Data;

@Data
public class RestResp {
    private String code;
    private String message;
    private Object data;
}
