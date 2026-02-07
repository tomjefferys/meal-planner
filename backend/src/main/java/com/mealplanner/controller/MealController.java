package com.mealplanner.controller;

import com.mealplanner.model.Meal;
import com.mealplanner.service.MealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public List<Meal> getAll(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return mealService.search(search);
        }
        return mealService.findAll();
    }

    @GetMapping("/{id}")
    public Meal getById(@PathVariable Long id) {
        return mealService.findById(id);
    }

    @PostMapping
    public Meal create(@RequestBody Meal meal) {
        return mealService.create(meal);
    }

    @PutMapping("/{id}")
    public Meal update(@PathVariable Long id, @RequestBody Meal meal) {
        return mealService.update(id, meal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public List<Meal> exportAll() {
        return mealService.findAll();
    }

    @PostMapping("/import")
    public Map<String, Object> importMeals(@RequestBody List<Meal> meals) {
        return mealService.importMeals(meals);
    }
}
