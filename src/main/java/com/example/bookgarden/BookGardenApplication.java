package com.example.bookgarden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BookGardenApplication {
	public static void main(String[] args) {
		SpringApplication.run(BookGardenApplication.class, args);
	}
}

