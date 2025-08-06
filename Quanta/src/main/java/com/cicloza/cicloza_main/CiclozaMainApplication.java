package com.cicloza.cicloza_main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.cicloza.util.ColorLogger;

@SpringBootApplication
@EntityScan(basePackages = "com.cicloza.entity")
@EnableJpaRepositories(basePackages = "com.cicloza.repository")
@ComponentScan(basePackages = {"com.cicloza.controller", "com.cicloza.service", "com.cicloza.util", "com.cicloza.exception"})
public class CiclozaMainApplication {
	private static final ColorLogger logger = ColorLogger.getLogger(CiclozaMainApplication.class);

	/**
	 * This method is the entry point of the application.
	 * @param args .
	 */
	public static void main(String[] args) {
		SpringApplication.run(CiclozaMainApplication.class, args);
	}
}
