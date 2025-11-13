package yunhan.supplement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@EnableAsync
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@MapperScan("yunhan.supplement.mapper")
public class SupplementApplication {
	public static void main(String[] args) {
		SpringApplication.run(SupplementApplication.class, args);
	}
}
