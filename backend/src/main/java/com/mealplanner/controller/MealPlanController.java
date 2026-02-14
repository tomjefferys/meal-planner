package com.mealplanner.controller;

import com.mealplanner.dto.EntryRequest;
import com.mealplanner.dto.ShoppingItem;
import com.mealplanner.model.MealPlan;
import com.mealplanner.model.MealPlanEntry;
import com.mealplanner.service.MealPlanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @GetMapping
    public List<MealPlan> getAll() {
        return mealPlanService.findAll();
    }

    @GetMapping("/week")
    public MealPlan getWeekPlan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return mealPlanService.getOrCreateForWeek(date);
    }

    @GetMapping("/{id}")
    public MealPlan getById(@PathVariable @NonNull Long id) {
        return mealPlanService.findById(id);
    }

    @PostMapping("/{planId}/entries")
    public MealPlanEntry addEntry(@PathVariable @NonNull Long planId, @RequestBody @NonNull EntryRequest request) {
        return mealPlanService.addEntry(planId, request);
    }

    @PutMapping("/entries/{entryId}")
    public MealPlanEntry updateEntry(@PathVariable @NonNull Long entryId, @RequestBody @NonNull EntryRequest request) {
        return mealPlanService.updateEntry(entryId, request);
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> deleteEntry(@PathVariable @NonNull Long entryId) {
        mealPlanService.deleteEntry(entryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{planId}/shopping-list")
    public List<ShoppingItem> getShoppingList(@PathVariable @NonNull Long planId) {
        return mealPlanService.generateShoppingList(planId);
    }

    @GetMapping("/shopping-list")
    public List<ShoppingItem> getShoppingListByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return mealPlanService.generateShoppingListForDateRange(startDate, endDate);
    }

    @PutMapping("/{planId}/notes")
    public MealPlan updateDayNotes(@PathVariable @NonNull Long planId, @RequestBody EntryRequest request) {
        return mealPlanService.updateDayNotes(planId, request.getDayNotes());
    }
}
