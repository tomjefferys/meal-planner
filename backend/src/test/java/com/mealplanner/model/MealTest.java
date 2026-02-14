package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MealTest {

    @Test
    void defaultConstructor_initializesEmptyIngredients() {
        Meal meal = new Meal();
        assertThat(meal.getIngredients()).isNotNull().isEmpty();
    }

    @Test
    void settersAndGetters_workCorrectly() {
        Meal meal = new Meal();
        meal.setId(1L);
        meal.setTitle("Test Meal");
        meal.setDescription("A test description");
        meal.setPrepTimeMinutes(10);
        meal.setCookTimeMinutes(20);
        meal.setEffort(Effort.EASY);
        meal.setImageUrl("http://example.com/image.jpg");

        assertThat(meal.getId()).isEqualTo(1L);
        assertThat(meal.getTitle()).isEqualTo("Test Meal");
        assertThat(meal.getDescription()).isEqualTo("A test description");
        assertThat(meal.getPrepTimeMinutes()).isEqualTo(10);
        assertThat(meal.getCookTimeMinutes()).isEqualTo(20);
        assertThat(meal.getEffort()).isEqualTo(Effort.EASY);
        assertThat(meal.getImageUrl()).isEqualTo("http://example.com/image.jpg");
    }

    @Test
    void ingredients_canBeSetAndRetrieved() {
        Meal meal = new Meal();
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient("Flour", 500, "g"));
        ingredients.add(new Ingredient("Sugar", 200, "g"));
        meal.setIngredients(ingredients);

        assertThat(meal.getIngredients()).hasSize(2);
        assertThat(meal.getIngredients().get(0).getName()).isEqualTo("Flour");
    }
}
