package com.harding.meals.config;

import com.google.api.services.calendar.Calendar;
import com.harding.meals.service.ai.MealPlanGenerationService;
import com.harding.meals.service.calendar.CalendarService;
import com.harding.meals.service.image.ImageSearchService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test configuration that mocks external service dependencies.
 * Import this configuration in test classes that need mocked external services.
 *
 * Usage:
 * @Import(MockExternalServicesConfig.class)
 */
@TestConfiguration
public class MockExternalServicesConfig {

    /**
     * Mock Google Calendar API client
     */
    @MockitoBean
    private Calendar googleCalendar;

    /**
     * Mock Calendar Service (wraps Google Calendar API)
     */
    @MockitoBean
    private CalendarService calendarService;

    /**
     * Mock Image Search Service (Pexels API)
     */
    @MockitoBean
    private ImageSearchService imageSearchService;

    /**
     * Note: MealPlanGenerationService should be mocked at the test level
     * since it needs specific stubbing behavior per test
     */
}
