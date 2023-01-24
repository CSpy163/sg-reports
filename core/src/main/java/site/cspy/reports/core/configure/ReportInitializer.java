package site.cspy.reports.core.configure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import site.cspy.reports.core.ReportOperator;
import site.cspy.reports.core.domain.ReportInfo;
import site.cspy.reports.core.domain.ReportContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ReportInitializer {
    @jakarta.annotation.Resource
    ApplicationContext applicationContext;


    @Bean
    @Scope("singleton")
    public ReportContext projectManager() {
        ReportContext reportContext = new ReportContext();
        ClassLoader loader = getClass().getClassLoader();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
        try {
            // 读取所有依赖下的资源文件，注意写法（不带 * 则同名资源文件只会读取第一个）
            Resource[] resources = resolver.getResources("classpath*:info.yaml");
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            // Date 优化
            mapper.findAndRegisterModules();
            // 未知属性隐藏错误
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<ReportInfo> infos = new ArrayList<>();
            reportContext.setInfos(infos);
            for (Resource resource : resources) {
                // 动态 yaml 转实例（带前缀）
                ReportInfo reportInfo = mapper.readerFor(ReportInfo.class).at("/reports/info").readValue(resource.getInputStream());
                infos.add(reportInfo);
            }

            // 设置 operator
            reportContext.setOperatorMap(applicationContext.getBeansOfType(ReportOperator.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return reportContext;
    }
}
