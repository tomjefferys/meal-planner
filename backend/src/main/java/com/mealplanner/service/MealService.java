package com.mealplanner.service;

import com.mealplanner.model.Meal;
import com.mealplanner.repository.MealRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public List<Meal> findAll() {
        return mealRepository.findAll();
    }

    public Meal findById(@NonNull Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found with id: " + id));
    }

    public List<Meal> search(String query) {
        return mealRepository.findByTitleContainingIgnoreCase(query);
    }

    public Meal create(@NonNull Meal meal) {
        return mealRepository.save(meal);
    }

    public Meal update(@NonNull Long id, Meal updated) {
        Meal meal = findById(id);
        meal.setTitle(updated.getTitle());
        meal.setDescription(updated.getDescription());
        meal.setIngredients(updated.getIngredients());
        meal.setPrepTimeMinutes(updated.getPrepTimeMinutes());
        meal.setCookTimeMinutes(updated.getCookTimeMinutes());
        meal.setEffort(updated.getEffort());
        meal.setImageUrl(updated.getImageUrl());
        return mealRepository.save(meal);
    }

    public void delete(@NonNull Long id) {
        mealRepository.deleteById(id);
    }

    public Map<String, Object> importMeals(List<Meal> meals) {
        int imported = 0;
        int skipped = 0;
        for (Meal meal : meals) {
            boolean exists = mealRepository.findByTitleIgnoreCase(meal.getTitle()).isPresent();
            if (!exists) {
                meal.setId(null);
                mealRepository.save(meal);
                imported++;
            } else {
                skipped++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        return result;
    }
}
