package com.harding.meals.repository;

import com.harding.meals.entity.meal.Meal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

public interface MealRepository extends PagingAndSortingRepository<Meal, Long>, CrudRepository<Meal, Long> {

}
