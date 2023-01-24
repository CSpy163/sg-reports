package site.cspy.reports.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "site.cspy.reports")
public class BusinessWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessWebApplication.class, args);
    }

}
