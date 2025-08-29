package com.harding.meals.dto.ai;

import com.harding.meals.dto.calendar.CalendarEventDto;
import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.dto.plan.PlanDto;

import java.util.Date;
import java.util.List;

public record AiMealPlanGenerationRequest(
    Date weekStartDate,
    Date weekEndDate,
    List<MealDto> availableMeals,
    List<PlanDto> recentMealPlans,
    List<PlanDto> existingPlansForWeek,
    List<CalendarEventDto> calendarEvents,
    String prompt
) {}