package com.ugent.pidgeon;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class PidgeonApplication {

	@RequestMapping("/")
	public String home(){
		return "hello world form spring!";
	}

	public static void main(String[] args) {
		SpringApplication.run(PidgeonApplication.class, args);
	}

}
