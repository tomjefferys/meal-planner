package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class MealRatingTest {

    @Test
    void settersAndGetters_workCorrectly() {
        MealRating rating = new MealRating();
        rating.setId(1L);
        rating.setRating(4);
        rating.setComment("Great meal!");
        rating.setRatedDate(LocalDate.of(2025, 2, 10));

        Meal meal = new Meal();
        meal.setTitle("Pasta");
        rating.setMeal(meal);

        Person person = new Person();
        person.setName("Alice");
        rating.setPerson(person);

        assertThat(rating.getId()).isEqualTo(1L);
        assertThat(rating.getRating()).isEqualTo(4);
        assertThat(rating.getComment()).isEqualTo("Great meal!");
        assertThat(rating.getRatedDate()).isEqualTo(LocalDate.of(2025, 2, 10));
        assertThat(rating.getMeal().getTitle()).isEqualTo("Pasta");
        assertThat(rating.getPerson().getName()).isEqualTo("Alice");
    }
}
