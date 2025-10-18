package com.harding.meals.repository;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.plan.PlanMeal;
import org.springframework.data.repository.CrudRepository;

public interface PlanMealRepository extends CrudRepository<PlanMeal, Long> {

    void deleteByMeal(Meal meal);
}
