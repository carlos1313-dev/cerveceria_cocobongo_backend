package com.cocobongo.cerveceria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync   // Habilita el @Async en AuditService
public class CerveceriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CerveceriaApplication.class, args);
	}

}
