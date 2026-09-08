package com.eldersphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class EldersphereApplication {

	public static void main(String[] args) {
		SpringApplication.run(EldersphereApplication.class, args);
	}

}
