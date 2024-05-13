package com.harding.meals.config;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.Unit;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(
                Meal.class,
                Ingredient.class,
                Unit.class
        );
    }
}
