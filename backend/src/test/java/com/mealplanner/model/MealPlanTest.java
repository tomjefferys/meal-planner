package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class MealPlanTest {

    @Test
    void defaultConstructor_initializesEmptyCollections() {
        MealPlan plan = new MealPlan();

        assertThat(plan.getEntries()).isNotNull().isEmpty();
        assertThat(plan.getDayNotes()).isNotNull().isEmpty();
    }

    @Test
    void settersAndGetters_workCorrectly() {
        MealPlan plan = new MealPlan();
        plan.setId(1L);
        plan.setWeekStartDate(LocalDate.of(2025, 2, 8));

        assertThat(plan.getId()).isEqualTo(1L);
        assertThat(plan.getWeekStartDate()).isEqualTo(LocalDate.of(2025, 2, 8));
    }

    @Test
    void dayNotes_canBeSetAndRetrieved() {
        MealPlan plan = new MealPlan();
        Map<String, String> notes = new HashMap<>();
        notes.put("MONDAY", "Takeaway night");
        notes.put("FRIDAY", "Pizza night");
        plan.setDayNotes(notes);

        assertThat(plan.getDayNotes()).hasSize(2);
        assertThat(plan.getDayNotes().get("MONDAY")).isEqualTo("Takeaway night");
    }
}
