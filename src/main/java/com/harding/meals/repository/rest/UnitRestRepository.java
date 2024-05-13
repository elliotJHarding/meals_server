package com.harding.meals.repository.rest;

import com.harding.meals.entity.meal.ingredient.Unit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "units", path = "units")
public interface UnitRestRepository extends CrudRepository<Unit, Long> {
}
