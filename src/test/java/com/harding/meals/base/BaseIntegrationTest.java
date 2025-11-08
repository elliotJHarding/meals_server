package com.harding.meals.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harding.meals.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for all integration tests.
 * Provides:
 * - Full Spring Boot application context
 * - H2 in-memory database (via test application.properties)
 * - Transaction rollback after each test
 * - Common test utilities and repositories
 *
 * Note: Redis Test Container is disabled when Docker is not available.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;

    // Repositories - available to all tests
    @Autowired
    protected MealRepository mealRepository;

    @Autowired
    protected PlanRepository planRepository;

    @Autowired
    protected AppUserRepository appUserRepository;

    @Autowired
    protected FamilyGroupRepository familyGroupRepository;

    @Autowired
    protected ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    protected PlanMealRepository planMealRepository;

    @Autowired
    protected MealTagRepository mealTagRepository;

    @Autowired
    protected IngredientMetadataRepository ingredientMetadataRepository;

    @Autowired
    protected AccessTokenRepository accessTokenRepository;

    @Autowired
    protected ActiveCalendarRepository activeCalendarRepository;

    /**
     * Configure Redis properties for tests.
     * Uses localhost Redis if available, or test properties override.
     */
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Disable Redis session for tests
        registry.add("spring.session.store-type", () -> "none");
    }

    /**
     * Clean up test data before each test.
     * Override this method in subclasses to add custom cleanup logic.
     */
    @BeforeEach
    void baseSetUp() {
        // Transactional annotation will handle rollback,
        // but you can add additional cleanup here if needed
    }

    /**
     * Get the base URL for the test server
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port + "/api";
    }
}
