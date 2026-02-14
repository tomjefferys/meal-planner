package com.mealplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.model.Effort;
import com.mealplanner.model.Ingredient;
import com.mealplanner.model.Meal;
import com.mealplanner.service.MealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealService mealService;

    @Autowired
    private ObjectMapper objectMapper;

    private Meal sampleMeal;

    @BeforeEach
    void setUp() {
        sampleMeal = new Meal();
        sampleMeal.setId(1L);
        sampleMeal.setTitle("Spaghetti Bolognese");
        sampleMeal.setDescription("Classic pasta dish");
        sampleMeal.setPrepTimeMinutes(15);
        sampleMeal.setCookTimeMinutes(30);
        sampleMeal.setEffort(Effort.MEDIUM);
        sampleMeal.setIngredients(List.of(new Ingredient("Pasta", 500, "g")));
    }

    @Test
    void getAll_returnsAllMeals() throws Exception {
        when(mealService.findAll()).thenReturn(List.of(sampleMeal));

        mockMvc.perform(get("/api/meals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Spaghetti Bolognese")))
                .andExpect(jsonPath("$[0].effort", is("MEDIUM")))
                .andExpect(jsonPath("$[0].prepTimeMinutes", is(15)));
    }

    @Test
    void getAll_returnsEmptyListWhenNoMeals() throws Exception {
        when(mealService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/meals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAll_withSearchParameter() throws Exception {
        when(mealService.search("spaghetti")).thenReturn(List.of(sampleMeal));

        mockMvc.perform(get("/api/meals").param("search", "spaghetti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Spaghetti Bolognese")));
    }

    @Test
    void getAll_withBlankSearchReturnsAll() throws Exception {
        when(mealService.findAll()).thenReturn(List.of(sampleMeal));

        mockMvc.perform(get("/api/meals").param("search", "  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(mealService).findAll();
        verify(mealService, never()).search(any());
    }

    @Test
    void getById_returnsMeal() throws Exception {
        when(mealService.findById(1L)).thenReturn(sampleMeal);

        mockMvc.perform(get("/api/meals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Spaghetti Bolognese")))
                .andExpect(jsonPath("$.ingredients", hasSize(1)))
                .andExpect(jsonPath("$.ingredients[0].name", is("Pasta")));
    }

    @Test
    void getById_throwsWhenNotFound() throws Exception {
        when(mealService.findById(99L)).thenThrow(new RuntimeException("Meal not found"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/meals/99")));
    }

    @Test
    void create_createsMeal() throws Exception {
        when(mealService.create(any(Meal.class))).thenReturn(sampleMeal);

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMeal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Spaghetti Bolognese")));
    }

    @Test
    void update_updatesMeal() throws Exception {
        when(mealService.update(eq(1L), any(Meal.class))).thenReturn(sampleMeal);

        mockMvc.perform(put("/api/meals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleMeal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Spaghetti Bolognese")));
    }

    @Test
    void delete_returnNoContent() throws Exception {
        doNothing().when(mealService).delete(1L);

        mockMvc.perform(delete("/api/meals/1"))
                .andExpect(status().isNoContent());

        verify(mealService).delete(1L);
    }

    @Test
    void exportAll_returnsAllMeals() throws Exception {
        when(mealService.findAll()).thenReturn(List.of(sampleMeal));

        mockMvc.perform(get("/api/meals/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void importMeals_returnsImportResult() throws Exception {
        Map<String, Object> importResult = Map.of("imported", 3, "skipped", 1);
        when(mealService.importMeals(any())).thenReturn(importResult);

        mockMvc.perform(post("/api/meals/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(sampleMeal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported", is(3)))
                .andExpect(jsonPath("$.skipped", is(1)));
    }
}
