package com.harding.meals.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.service")
public class AiServiceProperties {
    private String baseUrl;
    private String mealPlanGenerationEndpoint;
    private int timeoutSeconds = 30;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMealPlanGenerationEndpoint() {
        return mealPlanGenerationEndpoint;
    }

    public void setMealPlanGenerationEndpoint(String mealPlanGenerationEndpoint) {
        this.mealPlanGenerationEndpoint = mealPlanGenerationEndpoint;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getFullMealPlanGenerationUrl() {
        return baseUrl + mealPlanGenerationEndpoint;
    }
}