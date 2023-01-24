package site.cspy.reports.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import site.cspy.reports.core.domain.ReportCompileContext;
import site.cspy.reports.core.domain.enums.ReportCompileStatus;
import site.cspy.reports.core.util.RenderUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public interface ReportOperator {

    Logger logger = LoggerFactory.getLogger(ReportOperator.class);

    /**
     * 根据前端参数获取用户报表参数
     * @param params 前端传入的参数
     * @return 用户报表参数
     */
    Map<String, Object> prepareParams(String... params);

    default void makeReport(String reportCode, ReportGeneratedInterface rgi, String... params) throws IOException {
        logger.info("开始创建编号为：【{}】的报表。", reportCode);
        RenderUtil renderUtil = new RenderUtil();
        // 生成临时编译目录
        Path compilePath = Files.createTempDirectory("sgr-");
        logger.info("【{}】生成临时目录【{}】", reportCode, compilePath);
        // 获取用户报表参数，用于后续渲染
        Map<String, Object> variables = prepareParams(params);
        // 复制模板并渲染目录
        renderUtil.renderFullTexPath(reportCode, compilePath, variables);
        logger.info("【{}】渲染完成。", reportCode);
        // 编译 Tex
        ReportCompileContext compileContext = compileTex(compilePath);
        logger.info("【{}】编译完成。", reportCode);
        // 回调
        rgi.callback(compilePath, compileContext.getReport(), compileContext.getLogFiles());
        logger.info("【{}】完成回调。", reportCode);
        // 回调处理完之后，清理编译目录
        FileSystemUtils.deleteRecursively(compilePath);
        logger.info("【{}】清理临时目录完成。", reportCode);
    }


    /**
     * 最终报表名称
     *
     * @return 报表名称，用于验证最终产物
     */
    String reportFileName();

    /**
     * 报表编译命令
     *
     * @return 报表编译命令数组
     */
    String[][] getCommands();


    /**
     * 编译报表
     *
     * @param compilePath 编译路径
     * @return 编译结果
     */
    default ReportCompileContext compileTex(Path compilePath) {
        // 执行命令
        String[][] commands = getCommands();
        ReportCompileContext compileContext = new ReportCompileContext(compilePath, commands.length);
        for (int i = 0; i < commands.length; i++) {
            String[] command = commands[i];
            // 每条命令的编译结果写入文件
            Path logFile = Path.of(compilePath.toString(), String.format("sgr-%d.log", (i + 1)));
            compileContext.getLogFiles()[i] = logFile.toFile();
            ProcessBuilder processBuilder = new ProcessBuilder(command).directory(compilePath.toFile());
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logFile.toFile());
            try {
                processBuilder.start().waitFor();
            } catch (InterruptedException | IOException e) {
                try {
                    Files.writeString(logFile, e.getMessage(), StandardOpenOption.APPEND);
                } catch (IOException ignored) {
                }
                compileContext.setStatus(ReportCompileStatus.COMMAND_ERROR);
            }
        }

        // 验证结果
        File report = Path.of(compilePath.toString(), reportFileName()).toFile();
        if (report.exists()) {
            compileContext.setReport(report);
        } else {
            compileContext.setStatus(ReportCompileStatus.REPORT_NOT_FOUND);
        }

        compileContext.setStatus(ReportCompileStatus.SUCCESS);
        return compileContext;
    }
}
