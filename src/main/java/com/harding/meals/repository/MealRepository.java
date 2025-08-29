package com.harding.meals.repository;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.meal.Meal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface MealRepository extends PagingAndSortingRepository<Meal, Long>, CrudRepository<Meal, Long> {

    List<Meal> findByUser(AppUser user);

    @Query("from Meal where user.id in (select id from AppUser where familyGroup.uuid = user.familyGroup.uuid)")
    List<Meal> findByFamilyGroup(AppUser user);

    @Query("from Meal where name = :name and user.id in (select id from AppUser where familyGroup.uuid = :#{#user.familyGroup.uuid})")
    Optional<Meal> findByNameAndFamilyGroup(String name, AppUser user);

}
