package com.harding.meals.controller;

import com.harding.meals.entity.meal.MealTag;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.repository.MealTagRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
public class MealTagController {

    MealTagRepository mealTagRepository;

    public MealTagController(MealTagRepository mealTagRepository) {
        this.mealTagRepository = mealTagRepository;
    }

    @GetMapping("/tags")
    public List<MealTag> all(@AuthenticationPrincipal AppUser appUser) {
        List<MealTag> defaultTags = mealTagRepository.findByUserId(null);
        List<MealTag> mealTags = mealTagRepository.findByUserId(appUser.getId());

        return Stream.concat(
                defaultTags.stream(),
                mealTags.stream()
        ).toList();
    }
}
