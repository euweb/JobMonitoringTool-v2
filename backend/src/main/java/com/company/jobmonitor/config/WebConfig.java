package com.company.jobmonitor.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Handle static resources
    registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");

    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

    // Handle React Router - forward all non-API routes to index.html
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(
            new PathResourceResolver() {
              @Override
              protected Resource getResource(String resourcePath, Resource location)
                  throws IOException {
                Resource requestedResource = location.createRelative(resourcePath);

                // If the requested resource exists, return it
                if (requestedResource.exists() && requestedResource.isReadable()) {
                  return requestedResource;
                }

                // If it's an API call, don't intercept
                if (resourcePath.startsWith("api/")) {
                  return null;
                }

                // If it's an actuator endpoint, don't intercept
                if (resourcePath.startsWith("actuator/")) {
                  return null;
                }

                // Otherwise, return index.html for React Router
                return new ClassPathResource("/static/index.html");
              }
            });
  }
}
