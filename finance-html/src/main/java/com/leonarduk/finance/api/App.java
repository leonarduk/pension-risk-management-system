package com.leonarduk.finance.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.leonarduk.finance.stockfeed.Instrument.InstrumentLoader;

@SpringBootApplication
public class App {
	public static void main(final String[] args) throws Exception {
		InstrumentLoader.getInstance().init("resources/data/instruments_list.csv");
		
		SpringApplication.run(App.class, args);
	}
	

}
