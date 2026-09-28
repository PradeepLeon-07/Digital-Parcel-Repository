package com.college.digitalparcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// This tells Spring Boot: "Start here, scan all sub-packages, auto-configure everything"
@SpringBootApplication
public class DigitalParcelApplication {

    public static void main(String[] args) {
        // This line boots up the entire Spring application context,
        // starts the embedded Tomcat server, and makes the app ready to receive requests
        SpringApplication.run(DigitalParcelApplication.class, args);
    }
}
