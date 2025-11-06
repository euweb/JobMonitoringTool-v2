package com.company.jobmonitor.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
  public static void main(String[] args) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    System.out.println("admin123: " + encoder.encode("admin123"));
    System.out.println("user123: " + encoder.encode("user123"));
    System.out.println("testpassword: " + encoder.encode("testpassword"));
  }
}
