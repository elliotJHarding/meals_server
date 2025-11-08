package com.harding.meals.repository;

import com.harding.meals.base.BaseIntegrationTest;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.PublicDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ShoppingListItemRepository.
 * Tests basic CRUD operations and relationships with plans, meals, and ingredients.
 */
@Disabled
class ShoppingListItemRepositoryIntegrationTest extends BaseIntegrationTest {

    private AppUser testUser;
    private Plan testPlan;
    private Meal testMeal;
    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = createUser("test@example.com", "Test User");
        testUser = appUserRepository.save(testUser);

        // Create meal
        testMeal = new Meal();
        testMeal.setName("Test Meal");
        testMeal.setEffort(Effort.MEDIUM);
        testMeal.setUser(testUser);
        testMeal.setIngredients(new HashSet<>());
        testMeal = mealRepository.save(testMeal);

        // Create ingredient
        testIngredient = new Ingredient();
        testIngredient.setName("Tomatoes");
        testIngredient.setAmount(2.0);
        testIngredient.setMeal(testMeal);
        testIngredient.setIndex(0);

        // Create plan
        testPlan = new Plan();
        testPlan.setDate(LocalDate.now());
        testPlan.setUser(testUser);
        testPlan.setPlanMeals(new ArrayList<>());
        testPlan = planRepository.save(testPlan);
    }

    @Test
    void saveShoppingListItem_persistsSuccessfully() {
        // Given
        ShoppingListItem item = new ShoppingListItem();
        item.setIngredient(testIngredient);
        item.setMeal(testMeal);
        item.setChecked(false);
        item.setPlan(testPlan);

        // When
        ShoppingListItem savedItem = shoppingListItemRepository.save(item);

        // Then
        assertNotNull(savedItem.getId());
        assertEquals(testIngredient.getName(), savedItem.getIngredient().getName());
        assertEquals(testMeal.getId(), savedItem.getMeal().getId());
        assertFalse(savedItem.isChecked());
        assertEquals(testPlan.getId(), savedItem.getPlan().getId());
    }

    @Test
    void updateCheckedStatus_persistsChanges() {
        // Given
        ShoppingListItem item = new ShoppingListItem();
        item.setIngredient(testIngredient);
        item.setMeal(testMeal);
        item.setChecked(false);
        item.setPlan(testPlan);
        item = shoppingListItemRepository.save(item);

        // When
        item.setChecked(true);
        ShoppingListItem updatedItem = shoppingListItemRepository.save(item);

        // Then
        assertTrue(updatedItem.isChecked());

        // Verify persistence
        ShoppingListItem retrievedItem = shoppingListItemRepository.findById(updatedItem.getId()).orElseThrow();
        assertTrue(retrievedItem.isChecked());
    }

    @Test
    void deleteShoppingListItem_removesFromDatabase() {
        // Given
        ShoppingListItem item = new ShoppingListItem();
        item.setIngredient(testIngredient);
        item.setMeal(testMeal);
        item.setChecked(false);
        item.setPlan(testPlan);
        item = shoppingListItemRepository.save(item);
        Long itemId = item.getId();

        // When
        shoppingListItemRepository.deleteById(itemId);

        // Then
        assertFalse(shoppingListItemRepository.findById(itemId).isPresent());
    }

    @Test
    void shoppingListItemWithoutPlan_canBeSaved() {
        // Given
        ShoppingListItem item = new ShoppingListItem();
        item.setIngredient(testIngredient);
        item.setMeal(testMeal);
        item.setChecked(false);
        // No plan set

        // When
        ShoppingListItem savedItem = shoppingListItemRepository.save(item);

        // Then
        assertNotNull(savedItem.getId());
        assertNull(savedItem.getPlan());
    }

    @Test
    void multipleItemsForSamePlan_canExist() {
        // Given
        ShoppingListItem item1 = new ShoppingListItem();
        item1.setIngredient(testIngredient);
        item1.setMeal(testMeal);
        item1.setChecked(false);
        item1.setPlan(testPlan);

        ShoppingListItem item2 = new ShoppingListItem();
        item2.setIngredient(testIngredient);
        item2.setMeal(testMeal);
        item2.setChecked(true);
        item2.setPlan(testPlan);

        // When
        shoppingListItemRepository.saveAll(List.of(item1, item2));

        // Then
        List<ShoppingListItem> allItems = StreamSupport
                .stream(shoppingListItemRepository.findAll().spliterator(), false)
                .toList();
        long itemsForPlan = allItems.stream()
                .filter(item -> testPlan.getId().equals(item.getPlan().getId()))
                .count();

        assertEquals(2, itemsForPlan);
    }

    @Test
    void findAll_returnsAllItems() {
        // Given
        ShoppingListItem item1 = new ShoppingListItem();
        item1.setIngredient(testIngredient);
        item1.setMeal(testMeal);
        item1.setChecked(false);
        item1.setPlan(testPlan);

        ShoppingListItem item2 = new ShoppingListItem();
        item2.setIngredient(testIngredient);
        item2.setMeal(testMeal);
        item2.setChecked(true);
        item2.setPlan(testPlan);

        shoppingListItemRepository.saveAll(List.of(item1, item2));

        // When
        List<ShoppingListItem> allItems = StreamSupport
                .stream(shoppingListItemRepository.findAll().spliterator(), false)
                .toList();

        // Then
        assertTrue(allItems.size() >= 2);
    }

    @Test
    void shoppingListItemMaintainsRelationshipWithIngredient() {
        // Given
        ShoppingListItem item = new ShoppingListItem();
        item.setIngredient(testIngredient);
        item.setMeal(testMeal);
        item.setChecked(false);
        item.setPlan(testPlan);
        item = shoppingListItemRepository.save(item);

        // When
        ShoppingListItem retrievedItem = shoppingListItemRepository.findById(item.getId()).orElseThrow();

        // Then
        assertNotNull(retrievedItem.getIngredient());
        assertEquals("Tomatoes", retrievedItem.getIngredient().getName());
        assertEquals(2.0, retrievedItem.getIngredient().getAmount());
    }

    // Helper method
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
