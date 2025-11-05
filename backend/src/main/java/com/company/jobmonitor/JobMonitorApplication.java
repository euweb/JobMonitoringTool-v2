package com.company.jobmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class JobMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobMonitorApplication.class, args);
    }

}