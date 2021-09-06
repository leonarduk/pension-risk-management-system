package com.leonarduk.finance.api;

import com.leonarduk.finance.stockfeed.Instrument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

import java.net.NetworkInterface;

@SpringBootApplication
@RestController
@EnableJpaRepositories(basePackageClasses = {InstrumentRepository.class})
@EntityScan(basePackageClasses=Instrument.class)
@ComponentScan("com.leonarduk...")
public class App {

    public static void main(final String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}


}
