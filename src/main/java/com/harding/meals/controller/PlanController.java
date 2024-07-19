package com.harding.meals.controller;

import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.dto.plan.GetPlansRequest;
import com.harding.meals.dto.plan.PlanDto;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.PlanMapper;
import com.harding.meals.repository.PlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@RestController
public class PlanController {

    private final PlanMapper planMapper;
    private final PlanRepository planRepository;

    public PlanController(PlanRepository planRepository, PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    @GetMapping("/plans/{start}/{end}")
    public List<PlanDto> getPlans(@PathVariable LocalDate start, @PathVariable LocalDate end, @AuthenticationPrincipal AppUser user) {
        {
            if (start == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be null");
            }
            if (end == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be null");
            }
            if (start.isAfter(end)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
            }
        }

        List<Plan> savedPlans = planRepository.findByDateBetweenAndUser(start, end, user);

        List<LocalDate> datesBetween = start
                .datesUntil(end.plusDays(1))
                .toList();

        Predicate<LocalDate> dateHasPlan = (date) -> savedPlans.stream()
                .anyMatch(plan -> plan.getDate().equals(date));

        Function<LocalDate, Plan> getPlan = (date) -> savedPlans.stream()
                .filter(plan -> plan.getDate().equals(date))
                .findFirst()
                .orElseThrow();

        Function<LocalDate, Plan> createEmptyPlan = (date) ->
            new Plan(date, user);

        return datesBetween.stream()
            .map(date ->
                dateHasPlan.test(date) ?
                getPlan.apply(date) :
                createEmptyPlan.apply(date))
            .map(planMapper::toDto)
            .toList();
    }

    @PostMapping("/plans")
    PlanDto create(@RequestBody PlanDto mealPlanDto, @AuthenticationPrincipal AppUser user) {
        Plan plan = planMapper.toEntity(mealPlanDto);
        plan.setUser(user);
        return planMapper.toDto(planRepository.save(plan));
    }

    @PutMapping("/plans/{id}")
    void updateById(@RequestBody PlanDto newPlan, @PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        validateOwnership(user, id);

        Plan plan = planMapper.toEntity(newPlan);

        plan.setUser(user);
        plan.setId(id);

        planRepository.save(plan);
    }

    private void validateOwnership(AppUser user, long id) {
        Plan plan = planRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Plan does not exist"));

        if (!Objects.equals(plan.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("User does not own this plan");
        }
    }

}
