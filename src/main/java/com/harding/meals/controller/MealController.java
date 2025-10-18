package com.harding.meals.controller;

import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.mapping.MealMapper;
import com.harding.meals.repository.MealRepository;
import com.harding.meals.repository.PlanMealRepository;
import com.harding.meals.service.IngredientService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@RestController
public class MealController {

    private final PlanMealRepository planMealRepository;
    MealRepository mealRepository;
    MealMapper mapper;
    IngredientService ingredientService;
    EntityManager entityManager;

    public MealController(MealRepository mealRepository, MealMapper mapper, IngredientService ingredientService, EntityManager entityManager, PlanMealRepository planMealRepository) {
        this.mealRepository = mealRepository;
        this.mapper = mapper;
        this.ingredientService = ingredientService;
        this.entityManager = entityManager;
        this.planMealRepository = planMealRepository;
    }

    @GetMapping("/meals")
    Iterable<MealDto> all(Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        boolean inFamilyGroup = nonNull(user.getFamilyGroup());

        List<Meal> meals = inFamilyGroup ?
                mealRepository.findByFamilyGroup(user) :
                mealRepository.findByUser(user);

        return meals.stream()
                .map(meal -> mapper.toDto(meal))
                .collect(Collectors.toSet());
    }

    @PostMapping("/meals")
    MealDto create(@RequestBody MealDto mealDto, @AuthenticationPrincipal AppUser user) {
        Meal meal = mapper.toEntity(mealDto);
        meal.setUser(user);

        if (meal.getImage() != null) {
            meal.getImage().setMeal(meal);
        }

        meal = mealRepository.save(meal);
        return mapper.toDto(meal);
    }

    @GetMapping("/meals/{id}")
    MealDto findById(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        validateOwnership(user, id);
        return mapper.toDto(mealRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new));
    }

    @PutMapping("/meals/{id}")
    void updateById(@RequestBody MealDto newMeal, @PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        validateOwnership(user, id);

        Meal meal = mapper.toEntity(newMeal);

        meal.setUser(user);
        meal.setId(id);

        meal.getIngredients().forEach(ingredient -> {
            Meal parentMeal = new Meal();
            parentMeal.setId(meal.getId());
            ingredient.setMeal(parentMeal);
//            if (ingredient.getId() == 0) {
//                ingredient.setId(null);
//            }
        });

        ingredientService.enrichWithIngredientMetadata(meal);

        mealRepository.save(meal);
    }

    @Transactional
    @DeleteMapping("/meals/{id}")
    void deleteById(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        validateOwnership(user, id);

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meal does not exist"));

        planMealRepository.deleteByMeal(meal);

        mealRepository.deleteById(id);
    }

    private void validateOwnership(AppUser user, long id) {
        Meal meal = mealRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Meal does not exist"));

        boolean userOwnsMeal = !Objects.equals(meal.getUser().getId(), user.getId());

        boolean userInFamilyGroupOwnsMeal =
                user.getFamilyGroup() != null &&
                user.getFamilyGroup().getUuid().equals(
                        meal.getUser().getFamilyGroup().getUuid()
                );

        if (userOwnsMeal || userInFamilyGroupOwnsMeal) {
            return;
        }

        throw new IllegalArgumentException("User does not own this meal");
    }
}
