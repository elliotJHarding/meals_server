package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record DayMealPlanChatResponse(
        List<SuggestedMealDto> suggestions,
        String reasoning,
        @JsonProperty("conversationComplete")
        boolean conversationComplete,
        @JsonProperty("updatedChatContext")
        Map<String, Object> updatedChatContext
) {
}
