package com.mealplanner.dto;

public class ShoppingItem {

    private String name;
    private double totalQuantity;
    private String unit;

    public ShoppingItem() {
    }

    public ShoppingItem(String name, double totalQuantity, String unit) {
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
