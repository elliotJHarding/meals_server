package com.harding.meals.util;

import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.MealTag;
import com.harding.meals.entity.meal.Recipe;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.Unit;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fluent builder for creating test data entities.
 * Provides convenient methods to construct test entities with sensible defaults.
 */
public class TestDataBuilder {

    private static final AtomicLong idCounter = new AtomicLong(1000);

    // AppUser Builder
    public static class AppUserBuilder {
        private final AppUser user = new AppUser();
        private final com.harding.meals.entity.user.PublicDetails publicDetails =
            new com.harding.meals.entity.user.PublicDetails();

        public AppUserBuilder() {
            String email = "test" + idCounter.incrementAndGet() + "@example.com";
            user.setEmail(email);
            user.setUsername("user_" + System.nanoTime());
            publicDetails.setName("Test User");
            publicDetails.setEmailVerified(true);
            user.setPublicDetails(publicDetails);
        }

        public AppUserBuilder email(String email) {
            user.setEmail(email);
            return this;
        }

        public AppUserBuilder username(String username) {
            user.setUsername(username);
            return this;
        }

        public AppUserBuilder name(String name) {
            publicDetails.setName(name);
            return this;
        }

        public AppUserBuilder familyGroup(FamilyGroup familyGroup) {
            user.setFamilyGroup(familyGroup);
            return this;
        }

        public AppUser build() {
            return user;
        }
    }

    // FamilyGroup Builder
    public static class FamilyGroupBuilder {
        private final FamilyGroup familyGroup = new FamilyGroup();

        public FamilyGroupBuilder() {
            // UUID is auto-generated, no name field exists
        }

        public FamilyGroup build() {
            return familyGroup;
        }
    }

    // Meal Builder
    public static class MealBuilder {
        private final Meal meal = new Meal();
        private final Set<Ingredient> ingredients = new HashSet<>();

        public MealBuilder() {
            meal.setName("Test Meal " + idCounter.incrementAndGet());
            meal.setEffort(Effort.MEDIUM);
            meal.setServes(4);
            meal.setPrepTimeMinutes(30);
            meal.setDescription("A delicious test meal");
        }

        public MealBuilder name(String name) {
            meal.setName(name);
            return this;
        }

        public MealBuilder effort(Effort effort) {
            meal.setEffort(effort);
            return this;
        }

        public MealBuilder user(AppUser user) {
            meal.setUser(user);
            return this;
        }

        public MealBuilder serves(Integer serves) {
            meal.setServes(serves);
            return this;
        }

        public MealBuilder prepTime(Integer minutes) {
            meal.setPrepTimeMinutes(minutes);
            return this;
        }

        public MealBuilder description(String description) {
            meal.setDescription(description);
            return this;
        }

        public MealBuilder addIngredient(String name, double amount, Unit unit) {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(name);
            ingredient.setAmount(amount);
            ingredient.setUnit(unit);
            ingredient.setIndex(ingredients.size());
            ingredient.setMeal(meal);
            ingredients.add(ingredient);
            return this;
        }

        public MealBuilder recipe(Recipe recipe) {
            meal.setRecipe(recipe);
            return this;
        }

        public MealBuilder addTag(MealTag tag) {
            if (meal.getTags() == null) {
                meal.setTags(new HashSet<>());
            }
            meal.getTags().add(tag);
            return this;
        }

        public Meal build() {
            meal.setIngredients(ingredients);
            return meal;
        }
    }

    // Recipe Builder
    public static class RecipeBuilder {
        private final Recipe recipe = new Recipe();

        public RecipeBuilder() {
            recipe.setUrl("https://example.com/recipe");
            recipe.setTitle("Test Recipe");
        }

        public RecipeBuilder url(String url) {
            recipe.setUrl(url);
            return this;
        }

        public RecipeBuilder title(String title) {
            recipe.setTitle(title);
            return this;
        }

        public Recipe build() {
            return recipe;
        }
    }

    // Plan Builder
    public static class PlanBuilder {
        private final Plan plan = new Plan();

        public PlanBuilder() {
            plan.setDate(LocalDate.now());
        }

        public PlanBuilder date(LocalDate date) {
            plan.setDate(date);
            return this;
        }

        public PlanBuilder user(AppUser user) {
            plan.setUser(user);
            return this;
        }

        public PlanBuilder note(String note) {
            plan.setNote(note);
            return this;
        }

        public Plan build() {
            return plan;
        }
    }

    // PlanMeal Builder
    public static class PlanMealBuilder {
        private final PlanMeal planMeal = new PlanMeal();

        public PlanMealBuilder() {
            planMeal.setRequiredServings(4);
        }

        public PlanMealBuilder plan(Plan plan) {
            planMeal.setPlan(plan);
            return this;
        }

        public PlanMealBuilder meal(Meal meal) {
            planMeal.setMeal(meal);
            return this;
        }

        public PlanMealBuilder requiredServings(Integer servings) {
            planMeal.setRequiredServings(servings);
            return this;
        }

        public PlanMeal build() {
            return planMeal;
        }
    }

    // ShoppingListItem Builder
    public static class ShoppingListItemBuilder {
        private final ShoppingListItem item = new ShoppingListItem();

        public ShoppingListItemBuilder() {
            item.setChecked(false);
        }

        public ShoppingListItemBuilder ingredient(Ingredient ingredient) {
            item.setIngredient(ingredient);
            return this;
        }

        public ShoppingListItemBuilder checked(Boolean checked) {
            item.setChecked(checked);
            return this;
        }

        public ShoppingListItemBuilder plan(Plan plan) {
            item.setPlan(plan);
            return this;
        }

        public ShoppingListItemBuilder meal(Meal meal) {
            item.setMeal(meal);
            return this;
        }

        public ShoppingListItem build() {
            return item;
        }
    }

    // MealTag Builder
    public static class MealTagBuilder {
        private final MealTag tag = new MealTag();

        public MealTagBuilder() {
            tag.setName("Test Tag " + idCounter.incrementAndGet());
        }

        public MealTagBuilder name(String name) {
            tag.setName(name);
            return this;
        }

        public MealTag build() {
            return tag;
        }
    }

    // Static factory methods
    public static AppUserBuilder appUser() {
        return new AppUserBuilder();
    }

    public static FamilyGroupBuilder familyGroup() {
        return new FamilyGroupBuilder();
    }

    public static MealBuilder meal() {
        return new MealBuilder();
    }

    public static RecipeBuilder recipe() {
        return new RecipeBuilder();
    }

    public static PlanBuilder plan() {
        return new PlanBuilder();
    }

    public static PlanMealBuilder planMeal() {
        return new PlanMealBuilder();
    }

    public static ShoppingListItemBuilder shoppingListItem() {
        return new ShoppingListItemBuilder();
    }

    public static MealTagBuilder mealTag() {
        return new MealTagBuilder();
    }
}
