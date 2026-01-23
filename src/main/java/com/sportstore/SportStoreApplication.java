package com.sportstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SportStoreApplication {

    public static void main(String[] args) {
        // Lệnh này kích hoạt Spring Boot
        SpringApplication.run(SportStoreApplication.class, args);
        
        // In ra dòng này để báo hiệu server đã chạy thành công
        System.out.println("=========================================================");
        System.out.println("   SPORT STORE SERVER ĐANG CHẠY");
        System.out.println("=========================================================");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://apexsports.site", "http://localhost:8080", "http://localhost:5500")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

}