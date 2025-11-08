package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.harding.meals.dto.calendar.CalendarEventDto;
import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.dto.plan.PlanDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DayMealPlanChatRequest(
        @JsonProperty("dayOfWeek")
        LocalDate dayOfWeek,
        @JsonProperty("calendarEvents")
        @JsonDeserialize(as = ArrayList.class)
        List<CalendarEventDto> calendarEvents,
        @JsonProperty("currentWeekPlan")
        @JsonDeserialize(as = ArrayList.class)
        List<PlanDto> currentWeekPlan,
        @JsonProperty("recentMealPlans")
        @JsonDeserialize(as = ArrayList.class)
        List<PlanDto> recentMealPlans,
        @JsonProperty("availableMeals")
        @JsonDeserialize(as = ArrayList.class)
        List<MealDto> availableMeals,
        @JsonProperty("conversationHistory")
        @JsonDeserialize(as = ArrayList.class)
        List<ChatMessageDto> conversationHistory,
        @JsonProperty("chatContext")
        @JsonDeserialize(as = HashMap.class)
        Map<String, Object> chatContext
) {
    // Use canonical constructor instead of compact constructor
    public DayMealPlanChatRequest(
            LocalDate dayOfWeek,
            List<CalendarEventDto> calendarEvents,
            List<PlanDto> currentWeekPlan,
            List<PlanDto> recentMealPlans,
            List<MealDto> availableMeals,
            List<ChatMessageDto> conversationHistory,
            Map<String, Object> chatContext
    ) {
        this.dayOfWeek = dayOfWeek;
        this.calendarEvents = calendarEvents != null ? calendarEvents : new ArrayList<>();
        this.currentWeekPlan = currentWeekPlan != null ? currentWeekPlan : new ArrayList<>();
        this.recentMealPlans = recentMealPlans != null ? recentMealPlans : new ArrayList<>();
        this.availableMeals = availableMeals != null ? availableMeals : new ArrayList<>();
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
        this.chatContext = chatContext != null ? chatContext : new HashMap<>();
    }
}
