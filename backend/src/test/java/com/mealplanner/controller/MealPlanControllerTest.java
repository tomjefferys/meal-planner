package com.mealplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.dto.EntryRequest;
import com.mealplanner.dto.ShoppingItem;
import com.mealplanner.model.*;
import com.mealplanner.service.MealPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(MealPlanController.class)
class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealPlanService mealPlanService;

    @Autowired
    private ObjectMapper objectMapper;

    private MealPlan samplePlan;

    @BeforeEach
    void setUp() {
        samplePlan = new MealPlan();
        samplePlan.setId(1L);
        samplePlan.setWeekStartDate(LocalDate.of(2025, 2, 8));
    }

    @Test
    void getAll_returnsAllPlans() throws Exception {
        when(mealPlanService.findAll()).thenReturn(List.of(samplePlan));

        mockMvc.perform(get("/api/meal-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].weekStartDate", is("2025-02-08")));
    }

    @Test
    void getWeekPlan_returnsPlanForDate() throws Exception {
        when(mealPlanService.getOrCreateForWeek(LocalDate.of(2025, 2, 10)))
                .thenReturn(samplePlan);

        mockMvc.perform(get("/api/meal-plans/week").param("date", "2025-02-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getById_returnsPlan() throws Exception {
        when(mealPlanService.findById(1L)).thenReturn(samplePlan);

        mockMvc.perform(get("/api/meal-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.weekStartDate", is("2025-02-08")));
    }

    @Test
    void addEntry_addsEntryToPlan() throws Exception {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setId(1L);
        entry.setDayOfWeek("MONDAY");
        entry.setMealType(MealType.DINNER);

        Meal meal = new Meal();
        meal.setId(1L);
        meal.setTitle("Pasta");
        entry.setMeal(meal);

        EntryRequest request = new EntryRequest();
        request.setMealId(1L);
        request.setDayOfWeek("MONDAY");
        request.setMealType("DINNER");

        when(mealPlanService.addEntry(eq(1L), any(EntryRequest.class))).thenReturn(entry);

        mockMvc.perform(post("/api/meal-plans/1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$.mealType", is("DINNER")));
    }

    @Test
    void updateEntry_updatesEntry() throws Exception {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setId(1L);
        entry.setDayOfWeek("WEDNESDAY");
        entry.setMealType(MealType.LUNCH);

        EntryRequest request = new EntryRequest();
        request.setDayOfWeek("WEDNESDAY");
        request.setMealType("LUNCH");

        when(mealPlanService.updateEntry(eq(1L), any(EntryRequest.class))).thenReturn(entry);

        mockMvc.perform(put("/api/meal-plans/entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek", is("WEDNESDAY")));
    }

    @Test
    void deleteEntry_returnsNoContent() throws Exception {
        doNothing().when(mealPlanService).deleteEntry(1L);

        mockMvc.perform(delete("/api/meal-plans/entries/1"))
                .andExpect(status().isNoContent());

        verify(mealPlanService).deleteEntry(1L);
    }

    @Test
    void getShoppingList_returnsItems() throws Exception {
        List<ShoppingItem> items = List.of(
                new ShoppingItem("Pasta", 500, "g"),
                new ShoppingItem("Tomato Sauce", 300, "ml")
        );
        when(mealPlanService.generateShoppingList(1L)).thenReturn(items);

        mockMvc.perform(get("/api/meal-plans/1/shopping-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Pasta")))
                .andExpect(jsonPath("$[0].totalQuantity", is(500.0)));
    }

    @Test
    void getShoppingListByDateRange_returnsItems() throws Exception {
        List<ShoppingItem> items = List.of(new ShoppingItem("Rice", 1000, "g"));
        when(mealPlanService.generateShoppingListForDateRange(
                LocalDate.of(2025, 2, 8), LocalDate.of(2025, 2, 14)))
                .thenReturn(items);

        mockMvc.perform(get("/api/meal-plans/shopping-list")
                        .param("startDate", "2025-02-08")
                        .param("endDate", "2025-02-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Rice")));
    }

    @Test
    void updateDayNotes_updatesNotes() throws Exception {
        samplePlan.getDayNotes().put("MONDAY", "Takeaway");
        when(mealPlanService.updateDayNotes(eq(1L), any())).thenReturn(samplePlan);

        EntryRequest request = new EntryRequest();
        request.setDayNotes(Map.of("MONDAY", "Takeaway"));

        mockMvc.perform(put("/api/meal-plans/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayNotes.MONDAY", is("Takeaway")));
    }
}
