package com.mealplanner.service;

import com.mealplanner.dto.EntryRequest;
import com.mealplanner.dto.ShoppingItem;
import com.mealplanner.model.*;
import com.mealplanner.repository.MealPlanEntryRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.MealRepository;
import com.mealplanner.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanEntryRepository entryRepository;
    private final MealRepository mealRepository;
    private final PersonRepository personRepository;

    public MealPlanService(MealPlanRepository mealPlanRepository,
                           MealPlanEntryRepository entryRepository,
                           MealRepository mealRepository,
                           PersonRepository personRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.entryRepository = entryRepository;
        this.mealRepository = mealRepository;
        this.personRepository = personRepository;
    }

    public List<MealPlan> findAll() {
        return mealPlanRepository.findAllByOrderByWeekStartDateDesc();
    }

    public MealPlan findById(Long id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal plan not found with id: " + id));
    }

    @Transactional
    public MealPlan getOrCreateForWeek(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return mealPlanRepository.findByWeekStartDate(weekStart)
                .orElseGet(() -> {
                    MealPlan plan = new MealPlan();
                    plan.setWeekStartDate(weekStart);
                    return mealPlanRepository.save(plan);
                });
    }

    @Transactional
    public MealPlanEntry addEntry(Long planId, EntryRequest request) {
        MealPlan plan = findById(planId);
        Meal meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        MealPlanEntry entry = new MealPlanEntry();
        entry.setMealPlan(plan);
        entry.setMeal(meal);
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setMealType(MealType.valueOf(request.getMealType()));

        if (request.getAssignedCookId() != null) {
            Person cook = personRepository.findById(request.getAssignedCookId())
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            entry.setAssignedCook(cook);
        }

        plan.getEntries().add(entry);
        mealPlanRepository.save(plan);
        return entry;
    }

    @Transactional
    public MealPlanEntry updateEntry(Long entryId, EntryRequest request) {
        MealPlanEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (request.getDayOfWeek() != null) {
            entry.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getMealType() != null) {
            entry.setMealType(MealType.valueOf(request.getMealType()));
        }
        if (request.getMealId() != null) {
            Meal meal = mealRepository.findById(request.getMealId())
                    .orElseThrow(() -> new RuntimeException("Meal not found"));
            entry.setMeal(meal);
        }
        if (request.getAssignedCookId() != null) {
            Person cook = personRepository.findById(request.getAssignedCookId())
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            entry.setAssignedCook(cook);
        }

        return entryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long entryId) {
        entryRepository.deleteById(entryId);
    }

    public List<ShoppingItem> generateShoppingList(Long planId) {
        MealPlan plan = findById(planId);
        Map<String, ShoppingItem> items = new LinkedHashMap<>();

        for (MealPlanEntry entry : plan.getEntries()) {
            if (entry.getMeal() == null || entry.getMeal().getIngredients() == null) continue;

            for (Ingredient ingredient : entry.getMeal().getIngredients()) {
                String key = ingredient.getName().toLowerCase().trim()
                        + "|" + ingredient.getUnit().toLowerCase().trim();

                items.merge(key,
                        new ShoppingItem(ingredient.getName(), ingredient.getQuantity(), ingredient.getUnit()),
                        (existing, newItem) -> {
                            existing.setTotalQuantity(existing.getTotalQuantity() + newItem.getTotalQuantity());
                            return existing;
                        });
            }
        }

        List<ShoppingItem> result = new ArrayList<>(items.values());
        result.sort(Comparator.comparing(ShoppingItem::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
