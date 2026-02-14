package com.mealplanner.service;

import com.mealplanner.model.Effort;
import com.mealplanner.model.Ingredient;
import com.mealplanner.model.Meal;
import com.mealplanner.repository.MealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;

    @InjectMocks
    private MealService mealService;

    private Meal sampleMeal;

    @BeforeEach
    void setUp() {
        sampleMeal = new Meal();
        sampleMeal.setId(1L);
        sampleMeal.setTitle("Spaghetti Bolognese");
        sampleMeal.setDescription("Classic Italian pasta dish");
        sampleMeal.setPrepTimeMinutes(15);
        sampleMeal.setCookTimeMinutes(30);
        sampleMeal.setEffort(Effort.MEDIUM);
        sampleMeal.setIngredients(List.of(
                new Ingredient("Spaghetti", 500, "g"),
                new Ingredient("Minced Beef", 400, "g")
        ));
    }

    @Test
    void findAll_returnsAllMeals() {
        List<Meal> meals = List.of(sampleMeal);
        when(mealRepository.findAll()).thenReturn(meals);

        List<Meal> result = mealService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spaghetti Bolognese");
        verify(mealRepository).findAll();
    }

    @Test
    void findAll_returnsEmptyListWhenNoMeals() {
        when(mealRepository.findAll()).thenReturn(Collections.emptyList());

        List<Meal> result = mealService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsMealWhenFound() {
        when(mealRepository.findById(1L)).thenReturn(Optional.of(sampleMeal));

        Meal result = mealService.findById(1L);

        assertThat(result.getTitle()).isEqualTo("Spaghetti Bolognese");
        assertThat(result.getEffort()).isEqualTo(Effort.MEDIUM);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(mealRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meal not found with id: 99");
    }

    @Test
    void search_returnsMealsByTitle() {
        when(mealRepository.findByTitleContainingIgnoreCase("spaghetti"))
                .thenReturn(List.of(sampleMeal));

        List<Meal> result = mealService.search("spaghetti");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spaghetti Bolognese");
    }

    @Test
    void create_savesMeal() {
        when(mealRepository.save(any(Meal.class))).thenReturn(sampleMeal);

        Meal result = mealService.create(sampleMeal);

        assertThat(result.getId()).isEqualTo(1L);
        verify(mealRepository).save(sampleMeal);
    }

    @Test
    void update_updatesExistingMeal() {
        Meal updated = new Meal();
        updated.setTitle("Updated Spaghetti");
        updated.setDescription("Updated description");
        updated.setPrepTimeMinutes(20);
        updated.setCookTimeMinutes(40);
        updated.setEffort(Effort.HARD);
        updated.setIngredients(List.of(new Ingredient("Pasta", 300, "g")));
        updated.setImageUrl("http://example.com/img.jpg");

        when(mealRepository.findById(1L)).thenReturn(Optional.of(sampleMeal));
        when(mealRepository.save(any(Meal.class))).thenReturn(sampleMeal);

        mealService.update(1L, updated);

        assertThat(sampleMeal.getTitle()).isEqualTo("Updated Spaghetti");
        assertThat(sampleMeal.getDescription()).isEqualTo("Updated description");
        assertThat(sampleMeal.getPrepTimeMinutes()).isEqualTo(20);
        assertThat(sampleMeal.getCookTimeMinutes()).isEqualTo(40);
        assertThat(sampleMeal.getEffort()).isEqualTo(Effort.HARD);
        assertThat(sampleMeal.getImageUrl()).isEqualTo("http://example.com/img.jpg");
        verify(mealRepository).save(sampleMeal);
    }

    @Test
    void update_throwsWhenMealNotFound() {
        when(mealRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealService.update(99L, new Meal()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meal not found");
    }

    @Test
    void delete_deletesMealById() {
        mealService.delete(1L);

        verify(mealRepository).deleteById(1L);
    }

    @Test
    void importMeals_importsNewMeals() {
        Meal newMeal = new Meal();
        newMeal.setTitle("New Meal");
        newMeal.setId(5L);

        when(mealRepository.findByTitleIgnoreCase("New Meal")).thenReturn(Optional.empty());
        when(mealRepository.save(any(Meal.class))).thenReturn(newMeal);

        Map<String, Object> result = mealService.importMeals(List.of(newMeal));

        assertThat(result.get("imported")).isEqualTo(1);
        assertThat(result.get("skipped")).isEqualTo(0);
        // Verify id was set to null before save
        verify(mealRepository).save(argThat(meal -> meal.getId() == null));
    }

    @Test
    void importMeals_skipsDuplicates() {
        Meal existingMeal = new Meal();
        existingMeal.setTitle("Spaghetti Bolognese");

        when(mealRepository.findByTitleIgnoreCase("Spaghetti Bolognese"))
                .thenReturn(Optional.of(sampleMeal));

        Map<String, Object> result = mealService.importMeals(List.of(existingMeal));

        assertThat(result.get("imported")).isEqualTo(0);
        assertThat(result.get("skipped")).isEqualTo(1);
        verify(mealRepository, never()).save(any());
    }

    @Test
    void importMeals_handlesEmptyList() {
        Map<String, Object> result = mealService.importMeals(Collections.emptyList());

        assertThat(result.get("imported")).isEqualTo(0);
        assertThat(result.get("skipped")).isEqualTo(0);
    }

    @Test
    void importMeals_handlesMixOfNewAndExisting() {
        Meal newMeal = new Meal();
        newMeal.setTitle("New Meal");
        Meal existingMeal = new Meal();
        existingMeal.setTitle("Spaghetti Bolognese");

        when(mealRepository.findByTitleIgnoreCase("New Meal")).thenReturn(Optional.empty());
        when(mealRepository.findByTitleIgnoreCase("Spaghetti Bolognese"))
                .thenReturn(Optional.of(sampleMeal));
        when(mealRepository.save(any(Meal.class))).thenReturn(newMeal);

        Map<String, Object> result = mealService.importMeals(List.of(newMeal, existingMeal));

        assertThat(result.get("imported")).isEqualTo(1);
        assertThat(result.get("skipped")).isEqualTo(1);
    }
}
