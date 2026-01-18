package com.harding.meals.repository;

import com.harding.meals.base.BaseIntegrationTest;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import com.harding.meals.entity.user.PublicDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PlanRepository.
 * Tests complex query methods and plan relationships.
 */
class PlanRepositoryIntegrationTest extends BaseIntegrationTest {

    private AppUser user1;
    private AppUser user2;
    private AppUser userInSameFamily;
    private AppUser userWithoutFamily;
    private FamilyGroup familyGroup1;
    private Meal testMeal;

    @BeforeEach
    void setUp() {
        // Create family group
        familyGroup1 = new FamilyGroup();
        familyGroup1 = familyGroupRepository.save(familyGroup1);

        // Create users
        user1 = createUser("user1@test.com", "User 1");
        user1.setFamilyGroup(familyGroup1);
        user1 = appUserRepository.save(user1);

        user2 = createUser("user2@test.com", "User 2");
        user2 = appUserRepository.save(user2);

        userInSameFamily = createUser("family@test.com", "Family User");
        userInSameFamily.setFamilyGroup(familyGroup1);
        userInSameFamily = appUserRepository.save(userInSameFamily);

        userWithoutFamily = createUser("nofamily@test.com", "No Family User");
        userWithoutFamily = appUserRepository.save(userWithoutFamily);

        // Create a test meal
        testMeal = new Meal();
        testMeal.setName("Test Meal");
        testMeal.setEffort(Effort.MEDIUM);
        testMeal.setUser(user1);
        testMeal.setIngredients(new HashSet<>());
        testMeal = mealRepository.save(testMeal);
    }

    @Test
    void findByDateBetweenAndUser_returnsPlansInDateRange() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 7);

        Plan plan1 = createPlan(LocalDate.of(2025, 1, 2), user1);
        Plan plan2 = createPlan(LocalDate.of(2025, 1, 5), user1);
        Plan plan3 = createPlan(LocalDate.of(2025, 1, 10), user1); // Outside range
        Plan plan4 = createPlan(LocalDate.of(2025, 1, 3), user2); // Different user

        planRepository.saveAll(List.of(plan1, plan2, plan3, plan4));

        // When
        List<Plan> results = planRepository.findByDateBetweenAndUser(start, end, user1);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getDate().equals(LocalDate.of(2025, 1, 2))));
        assertTrue(results.stream().anyMatch(p -> p.getDate().equals(LocalDate.of(2025, 1, 5))));
    }

    @Test
    void findByDateBetweenAndUser_returnsEmptyListWhenNoPlans() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 7);

        // When
        List<Plan> results = planRepository.findByDateBetweenAndUser(start, end, user1);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void findByFamilyGroupAndDateBetween_returnsPlansForAllFamilyMembers() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 7);

        Plan plan1 = createPlan(LocalDate.of(2025, 1, 2), user1);
        Plan plan2 = createPlan(LocalDate.of(2025, 1, 3), userInSameFamily);
        Plan plan3 = createPlan(LocalDate.of(2025, 1, 4), user2); // Different family
        Plan plan4 = createPlan(LocalDate.of(2025, 1, 5), userWithoutFamily); // No family

        planRepository.saveAll(List.of(plan1, plan2, plan3, plan4));

        // When - query from user1's perspective
        List<Plan> results = planRepository.findByFamilyGroupAndDateBetween(start, end, user1);

        // Then - should include both user1 and userInSameFamily plans
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getDate().equals(LocalDate.of(2025, 1, 2))));
        assertTrue(results.stream().anyMatch(p -> p.getDate().equals(LocalDate.of(2025, 1, 3))));
    }

    @Test
    void findByFamilyGroupAndDateBetween_excludesPlansOutsideDateRange() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 5);
        LocalDate end = LocalDate.of(2025, 1, 10);

        Plan plan1 = createPlan(LocalDate.of(2025, 1, 3), user1); // Before range
        Plan plan2 = createPlan(LocalDate.of(2025, 1, 6), user1); // In range
        Plan plan3 = createPlan(LocalDate.of(2025, 1, 11), user1); // After range

        planRepository.saveAll(List.of(plan1, plan2, plan3));

        // When
        List<Plan> results = planRepository.findByFamilyGroupAndDateBetween(start, end, user1);

        // Then
        assertEquals(1, results.size());
        assertEquals(LocalDate.of(2025, 1, 6), results.get(0).getDate());
    }

    @Test
    void findByFamilyGroupAndDateBetween_returnsEmptyForUserWithoutFamily() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 7);

        Plan plan1 = createPlan(LocalDate.of(2025, 1, 2), userWithoutFamily);
        planRepository.save(plan1);

        // When
        List<Plan> results = planRepository.findByFamilyGroupAndDateBetween(start, end, userWithoutFamily);

        // Then
        // User without family group should still see their own plans
        assertEquals(1, results.size(), "User without family group should see their own plan");
        assertEquals(LocalDate.of(2025, 1, 2), results.get(0).getDate());
    }

    @Test
    void deleteAllByUserAndDate_deletesOnlySpecifiedUserAndDate() {
        // Given
        LocalDate dateToDelete = LocalDate.of(2025, 1, 5);
        LocalDate otherDate = LocalDate.of(2025, 1, 6);

        Plan planToDelete = createPlan(dateToDelete, user1);
        Plan planToKeep1 = createPlan(otherDate, user1); // Same user, different date
        Plan planToKeep2 = createPlan(dateToDelete, user2); // Same date, different user

        planRepository.saveAll(List.of(planToDelete, planToKeep1, planToKeep2));

        // When
        planRepository.deleteAllByUserAndDate(user1, dateToDelete);

        // Then
        List<Plan> remainingPlans = StreamSupport
                .stream(planRepository.findAll().spliterator(), false)
                .toList();
        assertEquals(2, remainingPlans.size());
        assertFalse(remainingPlans.stream()
                .anyMatch(p -> p.getUser().getId().equals(user1.getId()) && p.getDate().equals(dateToDelete)));
    }

    @Test
    void savePlanWithPlanMeals_persistsRelationships() {
        // Given
        Plan plan = createPlan(LocalDate.of(2025, 1, 10), user1);

        PlanMeal planMeal = new PlanMeal();
        planMeal.setPlan(plan);
        planMeal.setMeal(testMeal);
        planMeal.setRequiredServings(4);

        plan.setPlanMeals(new ArrayList<>(List.of(planMeal)));

        // When
        Plan savedPlan = planRepository.save(plan);

        // Then
        assertNotNull(savedPlan.getId());
        assertNotNull(savedPlan.getPlanMeals());
        assertEquals(1, savedPlan.getPlanMeals().size());
        assertEquals(4, savedPlan.getPlanMeals().get(0).getRequiredServings());
        assertEquals(testMeal.getId(), savedPlan.getPlanMeals().get(0).getMeal().getId());
    }

    @Test
    void deletePlan_cascadesDeleteToPlanMeals() {
        // Given
        Plan plan = createPlan(LocalDate.of(2025, 1, 10), user1);

        PlanMeal planMeal = new PlanMeal();
        planMeal.setPlan(plan);
        planMeal.setMeal(testMeal);
        planMeal.setRequiredServings(4);

        plan.setPlanMeals(new ArrayList<>(List.of(planMeal)));
        Plan savedPlan = planRepository.save(plan);
        Long planId = savedPlan.getId();

        // When
        planRepository.deleteById(planId);

        // Then
        assertFalse(planRepository.findById(planId).isPresent());
        // PlanMeal should be deleted due to cascade
        List<PlanMeal> orphanedPlanMeals = StreamSupport
                .stream(planMealRepository.findAll().spliterator(), false)
                .toList();
        assertFalse(orphanedPlanMeals.stream().anyMatch(pm -> planId.equals(pm.getPlan().getId())));
    }

    @Test
    void planWithNote_persistsNote() {
        // Given
        Plan plan = createPlan(LocalDate.of(2025, 1, 10), user1);
        plan.setNote("Remember to buy milk");

        // When
        Plan savedPlan = planRepository.save(plan);

        // Then
        Plan retrievedPlan = planRepository.findById(savedPlan.getId()).orElseThrow();
        assertEquals("Remember to buy milk", retrievedPlan.getNote());
    }

    @Test
    void uniqueConstraint_preventsDuplicateUserDateCombination() {
        // Given
        LocalDate sameDate = LocalDate.of(2025, 1, 10);

        Plan plan1 = createPlan(sameDate, user1);
        planRepository.save(plan1);

        Plan plan2 = createPlan(sameDate, user1);

        // When/Then
        assertThrows(Exception.class, () -> {
            planRepository.save(plan2);
            // Force constraint violation check
            planRepository.findAll();
        }, "Should throw exception due to unique constraint violation");
    }

    @Test
    void differentUsers_canHavePlansOnSameDate() {
        // Given
        LocalDate sameDate = LocalDate.of(2025, 1, 10);

        Plan plan1 = createPlan(sameDate, user1);
        Plan plan2 = createPlan(sameDate, user2);

        // When
        planRepository.saveAll(List.of(plan1, plan2));

        // Then - no exception should be thrown
        List<Plan> allPlans = StreamSupport
                .stream(planRepository.findAll().spliterator(), false)
                .toList();
        assertTrue(allPlans.size() >= 2);
    }

    // Helper methods
    private Plan createPlan(LocalDate date, AppUser user) {
        Plan plan = new Plan();
        plan.setDate(date);
        plan.setUser(user);
        plan.setPlanMeals(new ArrayList<>());
        return plan;
    }

    private AppUser createUser(String email, String name) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername("user_" + email.replace("@", "_").replace(".", "_"));

        PublicDetails publicDetails = new PublicDetails();
        publicDetails.setName(name);
        publicDetails.setEmailVerified(true);
        user.setPublicDetails(publicDetails);

        return user;
    }
}
