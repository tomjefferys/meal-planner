package com.mealplanner.service;

import com.mealplanner.model.Meal;
import com.mealplanner.repository.MealRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public List<Meal> findAll() {
        return mealRepository.findAll();
    }

    public Meal findById(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found with id: " + id));
    }

    public List<Meal> search(String query) {
        return mealRepository.findByTitleContainingIgnoreCase(query);
    }

    public Meal create(Meal meal) {
        return mealRepository.save(meal);
    }

    public Meal update(Long id, Meal updated) {
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

    public void delete(Long id) {
        mealRepository.deleteById(id);
    }
}
