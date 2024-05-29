package com.harding.meals.controller;

import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.entity.AppUser;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.mapping.MealMapper;
import com.harding.meals.repository.MealRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        return StreamSupport.stream(mealRepository.findByUser(user).spliterator(), false)
                .map(meal -> mapper.toDto(meal))
                .collect(Collectors.toSet());
    }

    @PostMapping("/meals")
    MealDto create(@RequestBody MealDto mealDto) {
        return mapper.toDto(mealRepository.save(mapper.toEntity(mealDto)));
    }

    @GetMapping("/meals/{id}")
    MealDto findById(@PathVariable Long id) {
        return mapper.toDto(mealRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new));
    }

    @PutMapping("/meals/{id}")
    void updateById(@RequestBody MealDto newMeal, @PathVariable Long id) {
        Meal meal = mapper.toEntity(newMeal);
        meal.getIngredients().forEach(ingredient -> {
            Meal parentMeal = new Meal();
            parentMeal.setId(meal.getId());
            ingredient.setMeal(parentMeal);
        });
        mealRepository.save(meal);
    }
}
