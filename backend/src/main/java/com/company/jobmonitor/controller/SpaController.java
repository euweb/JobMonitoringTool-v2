package com.company.jobmonitor.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController implements ErrorController {

  @RequestMapping("/error")
  public String handleError() {
    // Forward all 404 errors to index.html for React Router
    return "forward:/index.html";
  }
}
