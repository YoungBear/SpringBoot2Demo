package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class DemoApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping(value = "/demo/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> current() {
        ZonedDateTime now = ZonedDateTime.now();
        Map<String, String> result = new HashMap<>();
        result.put("zonedDateTime", now.toString());
        result.put("timestamp", String.valueOf(now.toInstant().toEpochMilli()));
        LOGGER.info("zonedDateTime: {}", now);
        LOGGER.info("timestamp: {}", now.toInstant().toEpochMilli());
        return result;
    }

}
