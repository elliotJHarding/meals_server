package com.harding.meals.repository;

import com.harding.meals.entity.meal.MealTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MealTagRepository extends CrudRepository<MealTag, Long> {

    public List<MealTag> findByUserId(Long userId);
}
