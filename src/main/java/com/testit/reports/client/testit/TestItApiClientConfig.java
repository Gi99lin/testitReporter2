package com.testit.reports.client.testit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class TestItApiClientConfig {

    private static final Logger LOGGER = Logger.getLogger(TestItApiClientConfig.class.getName());

    @Value("${testit.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient testItWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(String.format("Request: %s %s", clientRequest.method(), clientRequest.url()));
                clientRequest.headers().forEach((name, values) -> values.forEach(value -> LOGGER.info(String.format("%s: %s", name, value))));
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(String.format("Response status: %s", clientResponse.statusCode()));
            }
            return Mono.just(clientResponse);
        });
    }
}
