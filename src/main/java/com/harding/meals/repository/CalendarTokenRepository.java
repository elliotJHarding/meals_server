package com.harding.meals.repository;

import com.harding.meals.entity.user.CalendarToken;
import org.springframework.data.repository.CrudRepository;

public interface CalendarTokenRepository extends CrudRepository<CalendarToken, Long> {
}
