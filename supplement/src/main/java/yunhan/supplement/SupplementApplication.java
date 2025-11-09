package yunhan.supplement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@MapperScan("yunhan.supplement.mapper")
public class SupplementApplication {
	public static void main(String[] args) {
		SpringApplication.run(SupplementApplication.class, args);
	}
}
