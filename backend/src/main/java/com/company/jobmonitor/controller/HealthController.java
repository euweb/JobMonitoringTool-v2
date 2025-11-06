package com.company.jobmonitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello from Job Monitor API!";
  }

  @GetMapping("/status")
  public String status() {
    return "Job Monitor is running successfully!";
  }

  @GetMapping("/health")
  public String health() {
    return "OK - Application is healthy";
  }
}
