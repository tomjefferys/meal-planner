package com.mealplanner.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class MealPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meal_plan_id")
    @JsonBackReference
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "meal_id")
    private Meal meal;

    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_cook_id")
    private Person assignedCook;

    public MealPlanEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public Person getAssignedCook() {
        return assignedCook;
    }

    public void setAssignedCook(Person assignedCook) {
        this.assignedCook = assignedCook;
    }
}
