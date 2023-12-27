package ru.scraping.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScrapingApplication {

	private ScrapingApplication() {

	}

	public static void main(String[] args) {
		SpringApplication.run(ScrapingApplication.class, args);
	}

}