package com.ugent.pidgeon;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class PidgeonApplication {

	public static void main(String[] args) {
		SpringApplication.run(PidgeonApplication.class, args);
	}

}
