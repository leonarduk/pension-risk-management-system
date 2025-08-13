package com.leonarduk.finance.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.leonarduk.finance.springboot"})
@Import(AppConfig.class)
public class App {
	public static void main(final String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}


}
