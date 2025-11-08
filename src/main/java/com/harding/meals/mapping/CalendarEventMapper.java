package com.harding.meals.mapping;

import com.harding.meals.dto.calendar.CalendarEventDto;
import com.harding.meals.dto.calendar.CalendarEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CalendarEventMapper {
    CalendarEventMapper INSTANCE = Mappers.getMapper(CalendarEventMapper.class);

    CalendarEventDto toDto(CalendarEvent event);

    CalendarEvent toEntity(CalendarEventDto dto);
}