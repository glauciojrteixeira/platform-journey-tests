package com.nulote.platform_journey_tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nulote.journey", "com.nulote.platform_journey_tests"})
public class PlatformJourneyTestsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatformJourneyTestsApplication.class, args);
	}

}
