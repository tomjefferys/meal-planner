package com.mealplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.model.Person;
import com.mealplanner.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(PersonController.class)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    private Person samplePerson;

    @BeforeEach
    void setUp() {
        samplePerson = new Person();
        samplePerson.setId(1L);
        samplePerson.setName("Mum");
        samplePerson.setEatingPreferences("Vegetarian");
        samplePerson.setCookingPreferences("Enjoys baking");
    }

    @Test
    void getAll_returnsAllPeople() throws Exception {
        when(personService.findAll()).thenReturn(List.of(samplePerson));

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Mum")))
                .andExpect(jsonPath("$[0].eatingPreferences", is("Vegetarian")));
    }

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(personService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getById_returnsPerson() throws Exception {
        when(personService.findById(1L)).thenReturn(samplePerson);

        mockMvc.perform(get("/api/people/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mum")))
                .andExpect(jsonPath("$.eatingPreferences", is("Vegetarian")))
                .andExpect(jsonPath("$.cookingPreferences", is("Enjoys baking")));
    }

    @Test
    void getById_throwsWhenNotFound() throws Exception {
        when(personService.findById(99L)).thenThrow(new RuntimeException("Not found"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/people/99")));
    }

    @Test
    void create_createsPerson() throws Exception {
        when(personService.create(any(Person.class))).thenReturn(samplePerson);

        mockMvc.perform(post("/api/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePerson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mum")));
    }

    @Test
    void update_updatesPerson() throws Exception {
        when(personService.update(eq(1L), any(Person.class))).thenReturn(samplePerson);

        mockMvc.perform(put("/api/people/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePerson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mum")));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        doNothing().when(personService).delete(1L);

        mockMvc.perform(delete("/api/people/1"))
                .andExpect(status().isNoContent());

        verify(personService).delete(1L);
    }
}
