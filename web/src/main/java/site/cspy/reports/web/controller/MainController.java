package site.cspy.reports.web.controller;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import site.cspy.reports.core.ReportOperator;
import site.cspy.reports.web.base.RestResp;
import site.cspy.reports.core.domain.ReportContext;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/main")
public class MainController {

    @Resource
    ReportContext reportContext;

    @GetMapping("/listReports")
    public RestResp listReports() {
        RestResp restResp = new RestResp();
        restResp.setData(reportContext.getInfos());
        return restResp;
    }

    @GetMapping(value = "/makeReport", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] makeReport(@RequestParam("code") String code, @RequestParam(value = "params", required = false) List<String> params) {
        ReportOperator operator = reportContext.getOperatorMap().get(code);
        if (operator != null) {
            final byte[][] pdfBytes = {null};
            try {
                operator.makeReport(code, (compilePath, report, logFiles) -> {
                    try {
                        pdfBytes[0] = report == null ? new byte[0] : Files.readAllBytes(report.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, params == null ? new String[0] : params.toArray(new String[0]));
                return pdfBytes[0];
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new byte[0];
    }


}
