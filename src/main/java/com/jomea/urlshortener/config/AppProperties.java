package com.jomea.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    private String baseUrl = "http://localhost:8080";

    private int maxUrlLength = 2048;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getMaxUrlLength() {
        return maxUrlLength;
    }

    public void setMaxUrlLength(int maxUrlLength) {
        this.maxUrlLength = maxUrlLength;
    }
}
