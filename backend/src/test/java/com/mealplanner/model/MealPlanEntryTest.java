package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MealPlanEntryTest {

    @Test
    void settersAndGetters_workCorrectly() {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setId(1L);
        entry.setDayOfWeek("MONDAY");
        entry.setMealType(MealType.DINNER);
        entry.setDisplayOrder(2);

        Meal meal = new Meal();
        meal.setTitle("Pasta");
        entry.setMeal(meal);

        Person cook = new Person();
        cook.setName("Mum");
        entry.setAssignedCook(cook);

        MealPlan plan = new MealPlan();
        entry.setMealPlan(plan);

        assertThat(entry.getId()).isEqualTo(1L);
        assertThat(entry.getDayOfWeek()).isEqualTo("MONDAY");
        assertThat(entry.getMealType()).isEqualTo(MealType.DINNER);
        assertThat(entry.getDisplayOrder()).isEqualTo(2);
        assertThat(entry.getMeal().getTitle()).isEqualTo("Pasta");
        assertThat(entry.getAssignedCook().getName()).isEqualTo("Mum");
        assertThat(entry.getMealPlan()).isEqualTo(plan);
    }

    @Test
    void mealType_canBeNull() {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setMealType(null);

        assertThat(entry.getMealType()).isNull();
    }
}
