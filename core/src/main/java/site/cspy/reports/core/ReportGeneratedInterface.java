package site.cspy.reports.core;

import java.io.File;
import java.nio.file.Path;

public interface ReportGeneratedInterface {
    void callback(Path compilePath, File report, File[] logFiles);
}
