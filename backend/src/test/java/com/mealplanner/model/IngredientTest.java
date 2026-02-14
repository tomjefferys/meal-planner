package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class IngredientTest {

    @Test
    void parameterizedConstructor_setsAllFields() {
        Ingredient ingredient = new Ingredient("Flour", 500.0, "g");

        assertThat(ingredient.getName()).isEqualTo("Flour");
        assertThat(ingredient.getQuantity()).isEqualTo(500.0);
        assertThat(ingredient.getUnit()).isEqualTo("g");
    }

    @Test
    void defaultConstructor_leavesFieldsNull() {
        Ingredient ingredient = new Ingredient();

        assertThat(ingredient.getName()).isNull();
        assertThat(ingredient.getQuantity()).isEqualTo(0.0);
        assertThat(ingredient.getUnit()).isNull();
    }

    @Test
    void setters_updateFields() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setQuantity(250.0);
        ingredient.setUnit("g");

        assertThat(ingredient.getName()).isEqualTo("Sugar");
        assertThat(ingredient.getQuantity()).isEqualTo(250.0);
        assertThat(ingredient.getUnit()).isEqualTo("g");
    }
}
