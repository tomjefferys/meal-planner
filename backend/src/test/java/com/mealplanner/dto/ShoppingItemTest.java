package com.mealplanner.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShoppingItemTest {

    @Test
    void parameterizedConstructor_setsAllFields() {
        ShoppingItem item = new ShoppingItem("Pasta", 500.0, "g");

        assertThat(item.getName()).isEqualTo("Pasta");
        assertThat(item.getTotalQuantity()).isEqualTo(500.0);
        assertThat(item.getUnit()).isEqualTo("g");
    }

    @Test
    void defaultConstructor_leavesFieldsDefault() {
        ShoppingItem item = new ShoppingItem();

        assertThat(item.getName()).isNull();
        assertThat(item.getTotalQuantity()).isEqualTo(0.0);
        assertThat(item.getUnit()).isNull();
    }

    @Test
    void setters_updateFields() {
        ShoppingItem item = new ShoppingItem();
        item.setName("Rice");
        item.setTotalQuantity(1000.0);
        item.setUnit("g");

        assertThat(item.getName()).isEqualTo("Rice");
        assertThat(item.getTotalQuantity()).isEqualTo(1000.0);
        assertThat(item.getUnit()).isEqualTo("g");
    }
}
