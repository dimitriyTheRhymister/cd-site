package ru.job4j.site.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.job4j.site.handler.RestTemplateResponseErrorHandler;
import ru.job4j.site.util.RestAuthCall;

import javax.annotation.PostConstruct;

@Configuration
public class AppConfig {

    private final RestTemplate restTemplate;

    public AppConfig(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return restTemplate;
    }

    @PostConstruct
    public void init() {
        RestAuthCall.setRestTemplate(restTemplate);
    }
}