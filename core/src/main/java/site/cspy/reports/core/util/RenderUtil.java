package site.cspy.reports.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileSystemUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 报表渲染工具类
 */
public class RenderUtil {

    Logger logger = LoggerFactory.getLogger(RenderUtil.class);

    /**
     * regex 表达式，用于提取 thymeleaf 表达式
     */
    private static final Pattern thymeleafCommandPattern = Pattern.compile("%sgr:(?<expr>.*)");

    /**
     * 全局渲染引擎
     */
    private final TemplateEngine engine = new TemplateEngine();


    public RenderUtil() {
        // 解释器
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.TEXT);
        engine.setTemplateResolver(resolver);
    }

    /**
     * 渲染字符串
     *
     * @param templateStr 字符串模板
     * @param variables   变量
     * @return 渲染后字符串
     */
    public synchronized String render(String templateStr, Map<String, Object> variables) {
        Context context = new Context(Locale.getDefault());
        context.setVariables(variables);
        return engine.process(templateStr, context);
    }

    /**
     * 通过 Tex 的 InputStream 渲染
     *
     * @param texInputStream Tex 的 InputStream
     * @param variables      变量
     * @return 渲染后字符串
     */
    public synchronized String renderTex(InputStream texInputStream, Map<String, Object> variables) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(texInputStream))) {
            // 一次性读取 Tex 模板文件
            String template = String.join("\n", bufferedReader.lines().toList());

            StringBuilder finalStr = new StringBuilder();
            Matcher matcher = thymeleafCommandPattern.matcher(template);
            while (matcher.find()) {
                String expr = matcher.group("expr");
                String rendered = render(expr, variables);
                logger.debug("渲染字符串: {} -> {}", expr, rendered);
                // 防止 Group Replace，需要转义
                matcher.appendReplacement(finalStr, rendered.replace("$", "\\$"));
            }
            matcher.appendTail(finalStr);
            return finalStr.toString();
        }
    }

    /**
     * 封装方法
     * @param path 指定路径（Tex 文件）
     * @param variables 变量
     * @return 渲染后的文件
     * @throws IOException 错误信息
     */
    public String renderTex(Path path, Map<String, Object> variables) throws IOException {
        return renderTex(new FileInputStream(path.toFile()), variables);
    }


    /**
     * 检查目标目录
     * @param path 目录
     * @throws IOException 错误信息
     */
    private void checkPath(Path path) throws IOException {
        File pathFile = path.toFile();

        // 检查目标目录
        if (pathFile.exists()) {
            File[] files = pathFile.listFiles();
            if (files != null && files.length != 0) {
                throw new IOException(String.format("目标目录【%s】已存在且不为空！", path));
            }
        } else {
            if (!pathFile.mkdirs()) {
                throw new IOException(String.format("目标目录【%s】创建失败！", path));
            }
        }
    }



    /**
     * 整个目录渲染
     *
     * @param reportCode 报表编号
     * @param targetPath 【临时】目标目录
     * @param variables  变量
     * @throws IOException 读写错误
     */
    public synchronized void renderFullTexPath(String reportCode, Path targetPath, Map<String, Object> variables) throws IOException {
        checkPath(targetPath);

        // 按照 reportCode 获取资源文件
        ClassLoader loader = RenderUtil.class.getClassLoader();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
        Resource[] resources = resolver.getResources(reportCode);
        // 目前只检测第一个
        if (resources.length >= 1) {
            SgResourceFileUtil sgResourceFileUtil = new SgResourceFileUtil();
            sgResourceFileUtil.extractTo(reportCode, targetPath);

            // 遍历目录并渲染
            try (Stream<Path> walker = Files.walk(targetPath)) {
                for (Path path : walker.toList()) {
                    if (path.getFileName().toString().endsWith(".tex")) {
                        logger.debug("渲染文件: {}", path);
                        String rendered = renderTex(path, variables);
                        Files.writeString(path, rendered, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }
            }
        }
    }


}
