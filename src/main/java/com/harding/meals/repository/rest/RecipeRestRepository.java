package com.harding.meals.repository.rest;

import com.harding.meals.entity.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "recipes", path = "recipes")
public interface RecipeRestRepository extends PagingAndSortingRepository<Recipe, Long>, CrudRepository<Recipe, Long> {

    List<Recipe> findByName(@Param("name") String name);

}
