package com.harding.meals.repository;

import com.harding.meals.entity.AppUser;
import com.harding.meals.entity.meal.Meal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MealRepository extends PagingAndSortingRepository<Meal, Long>, CrudRepository<Meal, Long> {

    List<Meal> findByUser(AppUser user);

}
