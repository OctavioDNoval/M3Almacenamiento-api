package com.example.m3almacenamiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class M3AlmacenamientoApplication {

    public static void main(String[] args) {
        SpringApplication.run(M3AlmacenamientoApplication.class, args);
    }

}
