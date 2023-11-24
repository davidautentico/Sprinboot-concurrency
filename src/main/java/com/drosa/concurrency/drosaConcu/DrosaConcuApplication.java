package com.drosa.concurrency.drosaConcu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class DrosaConcuApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrosaConcuApplication.class, args);
	}

}
