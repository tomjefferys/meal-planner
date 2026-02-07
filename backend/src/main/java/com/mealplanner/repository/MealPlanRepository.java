package com.mealplanner.repository;

import com.mealplanner.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    Optional<MealPlan> findByWeekStartDate(LocalDate weekStartDate);
    List<MealPlan> findAllByOrderByWeekStartDateDesc();
}
