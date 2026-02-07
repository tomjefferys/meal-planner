package com.mealplanner.repository;

import com.mealplanner.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByTitleContainingIgnoreCase(String title);
}
