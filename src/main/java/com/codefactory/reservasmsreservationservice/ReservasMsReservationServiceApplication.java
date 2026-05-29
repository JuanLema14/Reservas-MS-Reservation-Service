package com.codefactory.reservasmsreservationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableFeignClients
@EnableMethodSecurity
@EnableAsync
public class ReservasMsReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasMsReservationServiceApplication.class, args);
    }

}
