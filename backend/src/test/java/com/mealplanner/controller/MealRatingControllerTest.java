package com.mealplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.dto.RatingRequest;
import com.mealplanner.model.Meal;
import com.mealplanner.model.MealRating;
import com.mealplanner.model.Person;
import com.mealplanner.service.MealRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealRatingController.class)
class MealRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealRatingService ratingService;

    @Autowired
    private ObjectMapper objectMapper;

    private MealRating sampleRating;

    @BeforeEach
    void setUp() {
        Meal meal = new Meal();
        meal.setId(1L);
        meal.setTitle("Pasta");

        Person person = new Person();
        person.setId(1L);
        person.setName("Alice");

        sampleRating = new MealRating();
        sampleRating.setId(1L);
        sampleRating.setMeal(meal);
        sampleRating.setPerson(person);
        sampleRating.setRating(4);
        sampleRating.setComment("Very good!");
        sampleRating.setRatedDate(LocalDate.of(2025, 2, 10));
    }

    @Test
    void getByMeal_returnsRatings() throws Exception {
        when(ratingService.findByMeal(1L)).thenReturn(List.of(sampleRating));

        mockMvc.perform(get("/api/ratings/meal/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating", is(4)))
                .andExpect(jsonPath("$[0].comment", is("Very good!")))
                .andExpect(jsonPath("$[0].person.name", is("Alice")));
    }

    @Test
    void getAverageRating_returnsAverage() throws Exception {
        when(ratingService.getAverageRating(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/ratings/meal/1/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealId", is(1)))
                .andExpect(jsonPath("$.averageRating", is(4.5)));
    }

    @Test
    void getAverageRating_returnsNullWhenNoRatings() throws Exception {
        when(ratingService.getAverageRating(1L)).thenReturn(null);

        mockMvc.perform(get("/api/ratings/meal/1/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealId", is(1)))
                .andExpect(jsonPath("$.averageRating").value(nullValue()));
    }

    @Test
    void create_createsRating() throws Exception {
        RatingRequest request = new RatingRequest();
        request.setMealId(1L);
        request.setPersonId(1L);
        request.setRating(4);
        request.setComment("Very good!");

        when(ratingService.create(any(RatingRequest.class))).thenReturn(sampleRating);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(4)))
                .andExpect(jsonPath("$.comment", is("Very good!")));
    }

    @Test
    void update_updatesRating() throws Exception {
        RatingRequest request = new RatingRequest();
        request.setRating(5);
        request.setComment("Even better!");

        sampleRating.setRating(5);
        sampleRating.setComment("Even better!");

        when(ratingService.update(eq(1L), any(RatingRequest.class))).thenReturn(sampleRating);

        mockMvc.perform(put("/api/ratings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Even better!")));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        doNothing().when(ratingService).delete(1L);

        mockMvc.perform(delete("/api/ratings/1"))
                .andExpect(status().isNoContent());

        verify(ratingService).delete(1L);
    }
}
