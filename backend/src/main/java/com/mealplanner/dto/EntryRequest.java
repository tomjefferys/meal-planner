package com.mealplanner.dto;

import java.util.Map;

public class EntryRequest {

    private Long mealId;
    private String dayOfWeek;
    private String mealType;
    private Long assignedCookId;
    private Map<String, String> dayNotes;

    public Long getMealId() {
        return mealId;
    }

    public void setMealId(Long mealId) {
        this.mealId = mealId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Long getAssignedCookId() {
        return assignedCookId;
    }

    public void setAssignedCookId(Long assignedCookId) {
        this.assignedCookId = assignedCookId;
    }

    public Map<String, String> getDayNotes() {
        return dayNotes;
    }

    public void setDayNotes(Map<String, String> dayNotes) {
        this.dayNotes = dayNotes;
    }
}
