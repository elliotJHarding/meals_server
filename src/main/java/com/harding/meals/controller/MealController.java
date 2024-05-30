package com.harding.meals.controller;

import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.entity.AppUser;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.mapping.MealMapper;
import com.harding.meals.repository.MealRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class MealController {

    MealRepository mealRepository;
    MealMapper mapper;

    public MealController(MealRepository mealRepository, MealMapper mapper) {
        this.mealRepository = mealRepository;
        this.mapper = mapper;
    }

    @GetMapping("/meals")
    Iterable<MealDto> all(Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();
        return mealRepository.findByUser(user).stream()
                .map(meal -> mapper.toDto(meal))
                .collect(Collectors.toSet());
    }

    @PostMapping("/meals")
    MealDto create(@RequestBody MealDto mealDto) {
        return mapper.toDto(mealRepository.save(mapper.toEntity(mealDto)));
    }

    @GetMapping("/meals/{id}")
    MealDto findById(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        validateOwnerShip(user, id);
        return mapper.toDto(mealRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new));
    }

    @PutMapping("/meals/{id}")
    void updateById(@RequestBody MealDto newMeal, @PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        validateOwnerShip(user, id);

        Meal meal = mapper.toEntity(newMeal);

        meal.setUser(user);
        meal.setId(id);

        meal.getIngredients().forEach(ingredient -> {
            Meal parentMeal = new Meal();
            parentMeal.setId(meal.getId());
            ingredient.setMeal(parentMeal);
        });

        mealRepository.save(meal);
    }

    private void validateOwnerShip(AppUser user, long id) {
        Meal meal = mealRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Meal does not exist"));

        if (!Objects.equals(meal.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("User does not own this meal");
        }

    }
}
