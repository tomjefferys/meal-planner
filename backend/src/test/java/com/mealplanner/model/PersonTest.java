package com.mealplanner.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PersonTest {

    @Test
    void settersAndGetters_workCorrectly() {
        Person person = new Person();
        person.setId(1L);
        person.setName("Alice");
        person.setEatingPreferences("Vegetarian");
        person.setCookingPreferences("Loves baking");

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getName()).isEqualTo("Alice");
        assertThat(person.getEatingPreferences()).isEqualTo("Vegetarian");
        assertThat(person.getCookingPreferences()).isEqualTo("Loves baking");
    }

    @Test
    void defaultConstructor_leavesFieldsNull() {
        Person person = new Person();

        assertThat(person.getId()).isNull();
        assertThat(person.getName()).isNull();
        assertThat(person.getEatingPreferences()).isNull();
        assertThat(person.getCookingPreferences()).isNull();
    }
}
