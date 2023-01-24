package site.cspy.reports.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SgResourceFileUtil {
    Logger logger = LoggerFactory.getLogger(SgResourceFileUtil.class);


    /**
     * 将源位置（可能在 Jar 中）复制到目标位置
     * @param sourcePath 源位置（可能在 Jar 中）
     * @param targetPath 目标位置
     * @throws IOException 读写错误
     */
    public void extractTo(String sourcePath, Path targetPath) throws IOException {
        logger.debug("extractTo: {} -> {}", sourcePath, targetPath);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 查找模板目录
        String pathInResource = String.format("%s%s", ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX, sourcePath);
        Resource[] folderInJars = resolver.getResources(pathInResource);
        // 只解析第一个
        if (folderInJars.length >= 1) {
            Resource folderInJar = folderInJars[0];
            URI folderInJarUri = folderInJar.getURI();
            // 如果是普通目录，则直接复制
            if ("file".equals(folderInJarUri.getScheme())) {
                FileSystemUtils.copyRecursively(Path.of(folderInJarUri).toFile(), targetPath.toFile());
            } else {
                // 否则则判断为 jar:file:
                Resource[] resources = resolver.getResources(pathInResource + "/**");
                for (Resource resource : resources) {
                    // 删除前缀，获取相对名称
                    String relativePath = resource.getURI().getRawSchemeSpecificPart().replace(folderInJarUri.getRawSchemeSpecificPart(), "");
                    // 创建新文件引用
                    File targetFile = Path.of(targetPath.toString(), relativePath).toFile();
                    if (relativePath.endsWith("/") || relativePath.endsWith("\\")) {
                        if (!targetFile.exists()) {
                            targetFile.mkdirs();
                        }
                    } else {
                        Files.copy(resource.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }
}
