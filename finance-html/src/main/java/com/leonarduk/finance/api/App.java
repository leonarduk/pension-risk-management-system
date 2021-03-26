package com.leonarduk.finance.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {
	public static void main(final String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}


}
