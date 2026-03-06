package com.thorugoh.soapingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SoapingestorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoapingestorApplication.class, args);
	}

}
