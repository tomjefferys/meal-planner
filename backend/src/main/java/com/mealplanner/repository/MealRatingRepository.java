package com.mealplanner.repository;

import com.mealplanner.model.MealRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MealRatingRepository extends JpaRepository<MealRating, Long> {
    List<MealRating> findByMealId(Long mealId);
    List<MealRating> findByPersonId(Long personId);

    @Query("SELECT AVG(r.rating) FROM MealRating r WHERE r.meal.id = :mealId")
    Double findAverageRatingByMealId(Long mealId);
}
