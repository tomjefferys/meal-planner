package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EnumTest {

    @Test
    void effort_hasCorrectValues() {
        assertThat(Effort.values()).containsExactly(Effort.EASY, Effort.MEDIUM, Effort.HARD);
    }

    @Test
    void effort_valueOfWorks() {
        assertThat(Effort.valueOf("EASY")).isEqualTo(Effort.EASY);
        assertThat(Effort.valueOf("MEDIUM")).isEqualTo(Effort.MEDIUM);
        assertThat(Effort.valueOf("HARD")).isEqualTo(Effort.HARD);
    }

    @Test
    void mealType_hasCorrectValues() {
        assertThat(MealType.values()).containsExactly(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);
    }

    @Test
    void mealType_valueOfWorks() {
        assertThat(MealType.valueOf("BREAKFAST")).isEqualTo(MealType.BREAKFAST);
        assertThat(MealType.valueOf("LUNCH")).isEqualTo(MealType.LUNCH);
        assertThat(MealType.valueOf("DINNER")).isEqualTo(MealType.DINNER);
    }
}
