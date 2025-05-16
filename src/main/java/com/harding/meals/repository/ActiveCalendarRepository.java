package com.harding.meals.repository;

import com.harding.meals.entity.user.ActiveCalendar;
import com.harding.meals.entity.user.AppUser;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ActiveCalendarRepository extends ListCrudRepository<ActiveCalendar, Long> {

    List<ActiveCalendar> findByUser(AppUser user);

}
