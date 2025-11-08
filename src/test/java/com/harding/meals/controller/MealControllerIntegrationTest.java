package com.harding.meals.controller;

import com.harding.meals.base.BaseControllerTest;
import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MealController.
 * Tests meal CRUD operations with authentication and authorization.
 */
class MealControllerIntegrationTest extends BaseControllerTest {

    private AppUser testUser;
    private AppUser otherUser;
    private AppUser familyMember;
    private FamilyGroup familyGroup;

    @BeforeEach
    void setUp() {
        super.baseSetUp();

        // Create family group
        familyGroup = createTestFamilyGroup("Test Family");

        // Create test users
        testUser = createTestUserWithFamily("test@example.com", "Test User", familyGroup);
        familyMember = createTestUserWithFamily("family@example.com", "Family Member", familyGroup);
        otherUser = createTestUser("other@example.com", "Other User");
    }

    @Test
    void getMeals_returnsUsersMeals() throws Exception {
        // Given
        Meal meal1 = createMeal("Meal 1", testUser);
        Meal meal2 = createMeal("Meal 2", testUser);
        mealRepository.save(meal1);
        mealRepository.save(meal2);

        // When/Then
        mockMvc.perform(authenticatedGet("/api/meals", testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItem("Meal 1")))
                .andExpect(jsonPath("$[*].name", hasItem("Meal 2")));
    }

    @Test
    void getMeals_includesFamilyGroupMeals() throws Exception {
        // Given
        Meal userMeal = createMeal("User Meal", testUser);
        Meal familyMeal = createMeal("Family Meal", familyMember);
        Meal otherMeal = createMeal("Other Meal", otherUser);

        mealRepository.save(userMeal);
        mealRepository.save(familyMeal);
        mealRepository.save(otherMeal);

        // When/Then
        mockMvc.perform(authenticatedGet("/api/meals", testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("User Meal")))
                .andExpect(jsonPath("$[*].name", hasItem("Family Meal")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Other Meal"))));
    }

    @Test
    void getMeals_requiresAuthentication() throws Exception {
        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/meals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMeal_savesSuccessfully() throws Exception {
        // Given
        MealDto mealDto = new MealDto(
                null,
                "New Meal",
                Effort.MEDIUM,
                null,
                "Delicious meal",
                4,
                30,
                null,
                null,
                null
        );

        // When/Then
        MvcResult result = mockMvc.perform(authenticatedPost("/api/meals", testUser)
                        .content(toJson(mealDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Meal"))
                .andExpect(jsonPath("$.effort").value("MEDIUM"))
                .andExpect(jsonPath("$.description").value("Delicious meal"))
                .andExpect(jsonPath("$.serves").value(4))
                .andExpect(jsonPath("$.prepTimeMinutes").value(30))
                .andReturn();

        // Verify meal was saved
        String responseJson = result.getResponse().getContentAsString();
        MealDto savedMeal = fromJson(responseJson, MealDto.class);
        assertNotNull(savedMeal.id());

        Meal dbMeal = mealRepository.findById(savedMeal.id()).orElseThrow();
        assertEquals("New Meal", dbMeal.getName());
        assertEquals(testUser.getId(), dbMeal.getUser().getId());
    }

    @Test
    void createMeal_requiresAuthentication() throws Exception {
        // Given
        MealDto mealDto = new MealDto(
                null,
                "New Meal",
                Effort.MEDIUM,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/meals")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(mealDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMealById_returnsOwnMeal() throws Exception {
        // Given
        Meal meal = createMeal("Test Meal", testUser);
        meal = mealRepository.save(meal);

        // When/Then
        mockMvc.perform(authenticatedGet("/api/meals/" + meal.getId(), testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(meal.getId()))
                .andExpect(jsonPath("$.name").value("Test Meal"));
    }

    @Test
    void getMealById_returnsFamilyMeal() throws Exception {
        // Given
        Meal meal = createMeal("Family Meal", familyMember);
        meal = mealRepository.save(meal);

        // When/Then
        mockMvc.perform(authenticatedGet("/api/meals/" + meal.getId(), testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Family Meal"));
    }

    @Test
    void getMealById_forbidsAccessToOtherUsersMeal() throws Exception {
        // Given
        Meal meal = createMeal("Other User Meal", otherUser);
        meal = mealRepository.save(meal);

        // When/Then
        mockMvc.perform(authenticatedGet("/api/meals/" + meal.getId(), testUser))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateMeal_updatesOwnMeal() throws Exception {
        // Given
        Meal meal = createMeal("Original Name", testUser);
        meal = mealRepository.save(meal);

        MealDto updatedDto = new MealDto(
                meal.getId(),
                "Updated Name",
                Effort.HIGH,
                null,
                "Updated description",
                6,
                45,
                null,
                null,
                null
        );

        // When/Then
        mockMvc.perform(authenticatedPut("/api/meals/" + meal.getId(), testUser)
                        .content(toJson(updatedDto)))
                .andExpect(status().isOk());

        // Verify update
        Meal updatedMeal = mealRepository.findById(meal.getId()).orElseThrow();
        assertEquals("Updated Name", updatedMeal.getName());
        assertEquals(Effort.HIGH, updatedMeal.getEffort());
        assertEquals("Updated description", updatedMeal.getDescription());
        assertEquals(6, updatedMeal.getServes());
        assertEquals(45, updatedMeal.getPrepTimeMinutes());
    }

    @Test
    void updateMeal_forbidsUpdatingOtherUsersMeal() throws Exception {
        // Given
        Meal meal = createMeal("Other User Meal", otherUser);
        meal = mealRepository.save(meal);

        MealDto updatedDto = new MealDto(
                meal.getId(),
                "Hacked Name",
                Effort.HIGH,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When/Then
        mockMvc.perform(authenticatedPut("/api/meals/" + meal.getId(), testUser)
                        .content(toJson(updatedDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteMeal_deletesOwnMeal() throws Exception {
        // Given
        Meal meal = createMeal("To Delete", testUser);
        meal = mealRepository.save(meal);
        Long mealId = meal.getId();

        // When/Then
        mockMvc.perform(authenticatedDelete("/api/meals/" + mealId, testUser))
                .andExpect(status().isOk());

        // Verify deletion
        assertFalse(mealRepository.findById(mealId).isPresent());
    }

    @Test
    void deleteMeal_forbidsDeletingOtherUsersMeal() throws Exception {
        // Given
        Meal meal = createMeal("Other User Meal", otherUser);
        meal = mealRepository.save(meal);

        // When/Then
        mockMvc.perform(authenticatedDelete("/api/meals/" + meal.getId(), testUser))
                .andExpect(status().is4xxClientError());

        // Verify meal still exists
        assertTrue(mealRepository.findById(meal.getId()).isPresent());
    }

    @Test
    void deleteMeal_returns400WhenMealDoesNotExist() throws Exception {
        // When/Then
        mockMvc.perform(authenticatedDelete("/api/meals/99999", testUser))
                .andExpect(status().is4xxClientError());
    }

    // Helper methods
    private Meal createMeal(String name, AppUser user) {
        Meal meal = new Meal();
        meal.setName(name);
        meal.setEffort(Effort.MEDIUM);
        meal.setUser(user);
        meal.setServes(4);
        meal.setPrepTimeMinutes(30);
        meal.setIngredients(new HashSet<>());
        return meal;
    }
}
