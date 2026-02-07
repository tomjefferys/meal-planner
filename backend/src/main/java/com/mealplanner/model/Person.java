package com.mealplanner.model;

import jakarta.persistence.*;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String eatingPreferences;

    @Column(length = 1000)
    private String cookingPreferences;

    public Person() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEatingPreferences() {
        return eatingPreferences;
    }

    public void setEatingPreferences(String eatingPreferences) {
        this.eatingPreferences = eatingPreferences;
    }

    public String getCookingPreferences() {
        return cookingPreferences;
    }

    public void setCookingPreferences(String cookingPreferences) {
        this.cookingPreferences = cookingPreferences;
    }
}
