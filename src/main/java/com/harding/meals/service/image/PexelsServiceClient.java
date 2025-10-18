package com.harding.meals.service.image;

import com.harding.meals.dto.image.PexelsSearchResponse;
import com.harding.meals.properties.PexelsServiceProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PexelsServiceClient {

    private final RestTemplate restTemplate;
    private final PexelsServiceProperties pexelsServiceProperties;

    public PexelsServiceClient(PexelsServiceProperties pexelsServiceProperties) {
        this.pexelsServiceProperties = pexelsServiceProperties;
        this.restTemplate = new RestTemplate();
    }

    public PexelsSearchResponse searchImages(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", pexelsServiceProperties.getApiKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(pexelsServiceProperties.getFullSearchUrl())
                .queryParam("query", query)
                .queryParam("per_page", pexelsServiceProperties.getPerPage())
                .toUriString();

        ResponseEntity<PexelsSearchResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PexelsSearchResponse.class
        );

        return response.getBody();
    }
}
