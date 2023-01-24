package site.cspy.reports.core.domain;

import lombok.Data;

import java.io.File;
import java.nio.file.Path;

@Data
public class ReportCompileContext {
    private int status;
    private Path compilePath;
    private File report;
    private File[] logFiles;

    public ReportCompileContext(Path compilePath, int logLength) {
        this.compilePath = compilePath;
        logFiles = new File[logLength];
    }
}
