package com.harding.meals.dto.plan;

import com.harding.meals.dto.DataTransferObject;

import java.time.LocalDate;

public record GetPlansRequest (
    LocalDate startDate,
    LocalDate endDate
) implements DataTransferObject {}
