package com.mealplanner.repository;

import com.mealplanner.model.MealPlanEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, Long> {
}
