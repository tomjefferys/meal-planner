package com.mealplanner.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RatingRequestTest {

    @Test
    void settersAndGetters_workCorrectly() {
        RatingRequest request = new RatingRequest();
        request.setMealId(1L);
        request.setPersonId(2L);
        request.setRating(5);
        request.setComment("Excellent!");

        assertThat(request.getMealId()).isEqualTo(1L);
        assertThat(request.getPersonId()).isEqualTo(2L);
        assertThat(request.getRating()).isEqualTo(5);
        assertThat(request.getComment()).isEqualTo("Excellent!");
    }
}
