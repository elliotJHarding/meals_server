package com.harding.meals.dto.ai;

import com.harding.meals.dto.DataTransferObject;

import java.util.Date;

public record GenerateMealPlanRequest(
    Date weekStartDate,
    Date weekEndDate,
    String prompt
) implements DataTransferObject {}