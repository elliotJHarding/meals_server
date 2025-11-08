package com.harding.meals.repository;

import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.user.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlanRepository extends PagingAndSortingRepository<Plan, Long>, CrudRepository<Plan, Long> {

    List<Plan> findByDateBetweenAndUser(LocalDate startDate, LocalDate endDate, AppUser user);

    @Query(
            """
            from Plan plan where plan.user = :user or
                plan.user.familyGroup in (
                    select u.familyGroup from AppUser u where u = :user
                )
                and plan.date >= :startDate
                and plan.date < :endDate
            """
    )
    List<Plan> findByFamilyGroupAndDateBetween(LocalDate startDate, LocalDate endDate, AppUser user);


    void deleteAllByUserAndDate(AppUser user, LocalDate date);
}
