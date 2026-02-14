package com.mealplanner.controller;

import com.mealplanner.dto.RatingRequest;
import com.mealplanner.model.MealRating;
import com.mealplanner.service.MealRatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class MealRatingController {

    private final MealRatingService ratingService;

    public MealRatingController(MealRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/meal/{mealId}")
    public List<MealRating> getByMeal(@PathVariable Long mealId) {
        return ratingService.findByMeal(mealId);
    }

    @GetMapping("/meal/{mealId}/average")
    public Map<String, Object> getAverageRating(@PathVariable Long mealId) {
        Double avg = ratingService.getAverageRating(mealId);
        Map<String, Object> result = new HashMap<>();
        result.put("mealId", mealId);
        result.put("averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : null);
        return result;
    }

    @PostMapping
    public MealRating create(@RequestBody @NonNull RatingRequest request) {
        return ratingService.create(request);
    }

    @PutMapping("/{id}")
    public MealRating update(@PathVariable @NonNull Long id, @RequestBody RatingRequest request) {
        return ratingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        ratingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
