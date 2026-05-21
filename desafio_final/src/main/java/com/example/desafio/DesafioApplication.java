package com.example.desafio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DesafioApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesafioApplication.class, args);
    }

}
