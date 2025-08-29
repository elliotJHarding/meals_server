package com.harding.meals.dto.ai;

import com.harding.meals.dto.plan.PlanDto;

import java.util.List;

public record AiMealPlanGenerationResponse(
    List<PlanDto> generatedPlans,
    String reasoning
) {}