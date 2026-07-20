package com.fileupload.fileproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FileprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileprojectApplication.class, args);
	}

}
