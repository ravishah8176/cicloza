package com.cicloza.cicloza_main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.cicloza.util.ColorLogger;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cicloza.controller", "com.cicloza.service"})
public class CiclozaMainApplication {
	private static final ColorLogger logger = ColorLogger.getLogger(CiclozaMainApplication.class);

	/**
	 * This method is the entry point of the application.
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(CiclozaMainApplication.class, args);
	}

	/**
	 * This bean is used to log the registered endpoints.
	 * @param mapping
	 * @return
	 */
	@Bean
	CommandLineRunner commandLineRunner(RequestMappingHandlerMapping mapping) {
		return args -> {
			logger.info("Registered endpoints:");
			mapping.getHandlerMethods().forEach((key, value) -> {
				logger.info("{} {}", key, value);
			});
		};
	}
}
