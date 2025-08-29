package com.harding.meals.service.ai;

import com.harding.meals.dto.ai.AiMealPlanGenerationRequest;
import com.harding.meals.dto.ai.AiMealPlanGenerationResponse;
import com.harding.meals.properties.AiServiceProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class AiServiceClient {
    
    private final RestTemplate restTemplate;
    private final AiServiceProperties aiServiceProperties;

    public AiServiceClient(AiServiceProperties aiServiceProperties) {
        this.aiServiceProperties = aiServiceProperties;
        this.restTemplate = new RestTemplate();
    }

    public AiMealPlanGenerationResponse generateMealPlan(AiMealPlanGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiMealPlanGenerationRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<AiMealPlanGenerationResponse> response = restTemplate.exchange(
            aiServiceProperties.getFullMealPlanGenerationUrl(),
            HttpMethod.POST,
            entity,
            AiMealPlanGenerationResponse.class
        );

        return response.getBody();
    }
}