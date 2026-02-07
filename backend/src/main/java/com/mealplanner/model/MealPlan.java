package com.mealplanner.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate weekStartDate;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<MealPlanEntry> entries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meal_plan_notes", joinColumns = @JoinColumn(name = "meal_plan_id"))
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "notes", columnDefinition = "TEXT")
    private Map<String, String> dayNotes = new HashMap<>();

    public MealPlan() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public List<MealPlanEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MealPlanEntry> entries) {
        this.entries = entries;
    }

    public Map<String, String> getDayNotes() {
        return dayNotes;
    }

    public void setDayNotes(Map<String, String> dayNotes) {
        this.dayNotes = dayNotes;
    }
}
