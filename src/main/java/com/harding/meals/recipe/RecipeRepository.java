package com.harding.meals.recipe;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "recipes", path = "recipes")
public interface RecipeRepository extends PagingAndSortingRepository<Recipe, Long>, CrudRepository<Recipe, Long> {

    List<Recipe> findByName(@Param("name") String name);

}
