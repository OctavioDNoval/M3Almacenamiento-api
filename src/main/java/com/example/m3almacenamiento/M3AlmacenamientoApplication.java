package com.example.m3almacenamiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableMethodSecurity
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class M3AlmacenamientoApplication {

    public static void main(String[] args) {
        SpringApplication.run(M3AlmacenamientoApplication.class, args);
    }

}
