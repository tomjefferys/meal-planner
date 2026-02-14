package com.mealplanner.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class EntryRequestTest {

    @Test
    void settersAndGetters_workCorrectly() {
        EntryRequest request = new EntryRequest();
        request.setMealId(1L);
        request.setDayOfWeek("MONDAY");
        request.setMealType("DINNER");
        request.setAssignedCookId(2L);
        request.setDisplayOrder(3);
        request.setDayNotes(Map.of("MONDAY", "Test note"));

        assertThat(request.getMealId()).isEqualTo(1L);
        assertThat(request.getDayOfWeek()).isEqualTo("MONDAY");
        assertThat(request.getMealType()).isEqualTo("DINNER");
        assertThat(request.getAssignedCookId()).isEqualTo(2L);
        assertThat(request.getDisplayOrder()).isEqualTo(3);
        assertThat(request.getDayNotes()).containsEntry("MONDAY", "Test note");
    }
}
