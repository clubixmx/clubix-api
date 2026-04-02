package com.clubix.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.clubix.api", "com.jmeta", "com.clubix.repository"})
@EnableR2dbcRepositories(basePackages = "com.clubix.repository.reactive")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
