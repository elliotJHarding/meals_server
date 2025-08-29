package com.harding.meals.service.ai;

import com.harding.meals.dto.ai.AiMealPlanGenerationRequest;
import com.harding.meals.dto.ai.AiMealPlanGenerationResponse;
import com.harding.meals.dto.calendar.CalendarEventDto;
import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.dto.plan.PlanDto;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.CalendarEventMapper;
import com.harding.meals.mapping.MealMapper;
import com.harding.meals.mapping.PlanMapper;
import com.harding.meals.repository.MealRepository;
import com.harding.meals.repository.PlanRepository;
import com.harding.meals.service.IngredientService;
import com.harding.meals.service.calendar.CalendarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MealPlanGenerationService {

    private final AiServiceClient aiServiceClient;
    private final MealRepository mealRepository;
    private final PlanRepository planRepository;
    private final CalendarService calendarService;
    private final IngredientService ingredientService;
    private final MealMapper mealMapper;
    private final PlanMapper planMapper;
    private final CalendarEventMapper calendarEventMapper;

    public MealPlanGenerationService(
            AiServiceClient aiServiceClient,
            MealRepository mealRepository,
            PlanRepository planRepository,
            CalendarService calendarService,
            IngredientService ingredientService,
            MealMapper mealMapper,
            PlanMapper planMapper,
            CalendarEventMapper calendarEventMapper) {
        this.aiServiceClient = aiServiceClient;
        this.mealRepository = mealRepository;
        this.planRepository = planRepository;
        this.calendarService = calendarService;
        this.ingredientService = ingredientService;
        this.mealMapper = mealMapper;
        this.planMapper = planMapper;
        this.calendarEventMapper = calendarEventMapper;
    }

    @Transactional
    public List<PlanDto> generateMealPlan(Date weekStartDate, Date weekEndDate, String prompt, AppUser user) 
            throws IOException, GeneralSecurityException {
        
        // Convert dates to LocalDate for internal processing
        LocalDate startDate = weekStartDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate endDate = weekEndDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        
        // Gather data for AI request
        List<MealDto> availableMeals = gatherAvailableMeals(user);
        List<PlanDto> recentPlans = gatherRecentMealPlans(user, startDate);
        List<PlanDto> existingPlansForWeek = gatherExistingPlansForWeek(user, startDate, endDate);
        List<CalendarEventDto> calendarEvents = gatherCalendarEvents(user, startDate, endDate);

        // Build AI request
        AiMealPlanGenerationRequest aiRequest = new AiMealPlanGenerationRequest(
            weekStartDate,
            weekEndDate,
            availableMeals,
            recentPlans,
            existingPlansForWeek,
            calendarEvents,
            prompt
        );

        // Call AI service
        AiMealPlanGenerationResponse aiResponse = aiServiceClient.generateMealPlan(aiRequest);

        // Save generated plans to database (only for dates without existing plans)
        List<PlanDto> savedPlans = saveGeneratedPlans(aiResponse.generatedPlans(), user, existingPlansForWeek);

        return savedPlans;
    }

    private List<MealDto> gatherAvailableMeals(AppUser user) {
        return mealRepository.findByFamilyGroup(user)
                .stream()
                .map(mealMapper::toDto)
                .toList();
    }

    private List<PlanDto> gatherRecentMealPlans(AppUser user, LocalDate weekStart) {
        LocalDate monthAgo = weekStart.minusMonths(1);
        LocalDate dayBeforeWeek = weekStart.minusDays(1);
        
        return planRepository.findByDateBetweenAndUser(monthAgo, dayBeforeWeek, user)
                .stream()
                .map(planMapper::toDto)
                .toList();
    }

    private List<PlanDto> gatherExistingPlansForWeek(AppUser user, LocalDate weekStart, LocalDate weekEnd) {
        return planRepository.findByDateBetweenAndUser(weekStart, weekEnd, user)
                .stream()
                .map(planMapper::toDto)
                .toList();
    }

    private List<CalendarEventDto> gatherCalendarEvents(AppUser user, LocalDate startDate, LocalDate endDate) 
            throws IOException, GeneralSecurityException {
        return calendarService.findAllEvents(user, startDate, endDate)
                .stream()
                .map(calendarEventMapper::toDto)
                .toList();
    }

    private List<PlanDto> saveGeneratedPlans(List<PlanDto> generatedPlans, AppUser user, List<PlanDto> existingPlansForWeek) {
        // Get dates that already have plans to avoid overwriting
        List<LocalDate> existingPlanDates = existingPlansForWeek.stream()
                .map(planDto -> planDto.date().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                .toList();
        
        // Filter generated plans to only include dates without existing plans
        List<PlanDto> newPlansToSave = generatedPlans.stream()
                .filter(planDto -> {
                    LocalDate planDate = planDto.date().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
                    return !existingPlanDates.contains(planDate);
                })
                .toList();
        
        List<PlanDto> savedPlans = newPlansToSave.stream()
                .map(planDto -> {
                    Plan plan = planMapper.toEntity(planDto);
                    plan.setUser(user);
                    
                    // Set plan reference for all plan meals and populate meal IDs
                    if (plan.getPlanMeals() != null) {
                        plan.getPlanMeals().forEach(planMeal -> {
                            planMeal.setPlan(plan);
                            
                            // Look up meal ID by name
                            if (planMeal.getMeal() != null && planMeal.getMeal().getName() != null) {
                                String mealName = planMeal.getMeal().getName();
                                mealRepository.findByNameAndFamilyGroup(mealName, user)
                                    .ifPresent(foundMeal -> planMeal.getMeal().setId(foundMeal.getId()));
                            }
                        });
                    }
                    
                    // Populate shopping list
                    ingredientService.populatePlanShoppingList(plan);
                    
                    // Save and return as DTO
                    Plan savedPlan = planRepository.save(plan);
                    return planMapper.toDto(savedPlan);
                })
                .toList();
        
        // Combine saved new plans with existing plans for the full week response
        List<PlanDto> allPlans = new ArrayList<>(savedPlans);
        allPlans.addAll(existingPlansForWeek);
        return allPlans;
    }
}