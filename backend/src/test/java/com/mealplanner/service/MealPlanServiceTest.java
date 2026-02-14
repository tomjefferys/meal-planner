package com.mealplanner.service;

import com.mealplanner.dto.EntryRequest;
import com.mealplanner.dto.ShoppingItem;
import com.mealplanner.model.*;
import com.mealplanner.repository.MealPlanEntryRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.MealRepository;
import com.mealplanner.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private MealPlanEntryRepository entryRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    private MealPlan samplePlan;
    private Meal sampleMeal;
    private Person samplePerson;

    @BeforeEach
    void setUp() {
        samplePlan = new MealPlan();
        samplePlan.setId(1L);
        samplePlan.setWeekStartDate(LocalDate.of(2025, 2, 8)); // A Saturday

        sampleMeal = new Meal();
        sampleMeal.setId(1L);
        sampleMeal.setTitle("Pasta");
        sampleMeal.setIngredients(List.of(
                new Ingredient("Pasta", 500, "g"),
                new Ingredient("Tomato Sauce", 300, "ml")
        ));

        samplePerson = new Person();
        samplePerson.setId(1L);
        samplePerson.setName("Mum");
    }

    @Test
    void findAll_returnsAllPlansOrderedByDate() {
        when(mealPlanRepository.findAllByOrderByWeekStartDateDesc())
                .thenReturn(List.of(samplePlan));

        List<MealPlan> result = mealPlanService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWeekStartDate()).isEqualTo(LocalDate.of(2025, 2, 8));
    }

    @Test
    void findById_returnsPlanWhenFound() {
        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        MealPlan result = mealPlanService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(mealPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meal plan not found");
    }

    @Test
    void getOrCreateForWeek_returnsExistingPlan() {
        LocalDate tuesday = LocalDate.of(2025, 2, 11); // Tuesday
        LocalDate saturday = LocalDate.of(2025, 2, 8); // Previous Saturday

        when(mealPlanRepository.findByWeekStartDate(saturday))
                .thenReturn(Optional.of(samplePlan));

        MealPlan result = mealPlanService.getOrCreateForWeek(tuesday);

        assertThat(result.getId()).isEqualTo(1L);
        verify(mealPlanRepository, never()).save(any());
    }

    @Test
    void getOrCreateForWeek_createsNewPlanWhenNoneExists() {
        LocalDate wednesday = LocalDate.of(2025, 2, 12);
        LocalDate saturday = LocalDate.of(2025, 2, 8);

        when(mealPlanRepository.findByWeekStartDate(saturday)).thenReturn(Optional.empty());
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(inv -> {
            MealPlan plan = inv.getArgument(0);
            plan.setId(2L);
            return plan;
        });

        MealPlan result = mealPlanService.getOrCreateForWeek(wednesday);

        assertThat(result.getWeekStartDate()).isEqualTo(saturday);
        verify(mealPlanRepository).save(any(MealPlan.class));
    }

    @Test
    void getOrCreateForWeek_handlesDateThatIsSaturday() {
        LocalDate saturday = LocalDate.of(2025, 2, 8);

        when(mealPlanRepository.findByWeekStartDate(saturday))
                .thenReturn(Optional.of(samplePlan));

        MealPlan result = mealPlanService.getOrCreateForWeek(saturday);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void addEntry_addsEntryToPlan() {
        EntryRequest request = new EntryRequest();
        request.setMealId(1L);
        request.setDayOfWeek("MONDAY");
        request.setMealType("DINNER");
        request.setAssignedCookId(1L);

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(mealRepository.findById(1L)).thenReturn(Optional.of(sampleMeal));
        when(personRepository.findById(1L)).thenReturn(Optional.of(samplePerson));
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(samplePlan);

        MealPlanEntry result = mealPlanService.addEntry(1L, request);

        assertThat(result.getDayOfWeek()).isEqualTo("MONDAY");
        assertThat(result.getMealType()).isEqualTo(MealType.DINNER);
        assertThat(result.getMeal()).isEqualTo(sampleMeal);
        assertThat(result.getAssignedCook()).isEqualTo(samplePerson);
        assertThat(result.getDisplayOrder()).isEqualTo(0);
        assertThat(samplePlan.getEntries()).hasSize(1);
    }

    @Test
    void addEntry_addsEntryWithoutMealType() {
        EntryRequest request = new EntryRequest();
        request.setMealId(1L);
        request.setDayOfWeek("TUESDAY");
        // No meal type

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(mealRepository.findById(1L)).thenReturn(Optional.of(sampleMeal));
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(samplePlan);

        MealPlanEntry result = mealPlanService.addEntry(1L, request);

        assertThat(result.getMealType()).isNull();
        assertThat(result.getAssignedCook()).isNull();
    }

    @Test
    void addEntry_throwsWhenMealNotFound() {
        EntryRequest request = new EntryRequest();
        request.setMealId(99L);
        request.setDayOfWeek("MONDAY");
        request.setMealType("DINNER");

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(mealRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.addEntry(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meal not found");
    }

    @Test
    void updateEntry_updatesExistingEntry() {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setId(1L);
        entry.setDayOfWeek("MONDAY");
        entry.setMealType(MealType.DINNER);

        EntryRequest request = new EntryRequest();
        request.setDayOfWeek("WEDNESDAY");
        request.setMealType("LUNCH");

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(entryRepository.save(any(MealPlanEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        MealPlanEntry result = mealPlanService.updateEntry(1L, request);

        assertThat(result.getDayOfWeek()).isEqualTo("WEDNESDAY");
        assertThat(result.getMealType()).isEqualTo(MealType.LUNCH);
    }

    @Test
    void updateEntry_throwsWhenEntryNotFound() {
        when(entryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.updateEntry(99L, new EntryRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entry not found");
    }

    @Test
    void deleteEntry_removesEntryFromPlan() {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setId(1L);
        entry.setMealPlan(samplePlan);
        samplePlan.getEntries().add(entry);

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(samplePlan);

        mealPlanService.deleteEntry(1L);

        assertThat(samplePlan.getEntries()).isEmpty();
        verify(mealPlanRepository).save(samplePlan);
    }

    @Test
    void generateShoppingList_combinesIngredients() {
        Meal meal1 = new Meal();
        meal1.setIngredients(List.of(
                new Ingredient("Pasta", 500, "g"),
                new Ingredient("Tomato Sauce", 300, "ml")
        ));

        Meal meal2 = new Meal();
        meal2.setIngredients(List.of(
                new Ingredient("Pasta", 400, "g"),
                new Ingredient("Cheese", 200, "g")
        ));

        MealPlanEntry entry1 = new MealPlanEntry();
        entry1.setMeal(meal1);
        MealPlanEntry entry2 = new MealPlanEntry();
        entry2.setMeal(meal2);
        samplePlan.getEntries().addAll(List.of(entry1, entry2));

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        List<ShoppingItem> result = mealPlanService.generateShoppingList(1L);

        // Should combine pasta quantities
        assertThat(result).hasSize(3);
        ShoppingItem pastaItem = result.stream()
                .filter(i -> i.getName().equalsIgnoreCase("Pasta"))
                .findFirst().orElseThrow();
        assertThat(pastaItem.getTotalQuantity()).isEqualTo(900.0);
    }

    @Test
    void generateShoppingList_handlesEmptyPlan() {
        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        List<ShoppingItem> result = mealPlanService.generateShoppingList(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void generateShoppingList_handlesNullMealInEntry() {
        MealPlanEntry entry = new MealPlanEntry();
        entry.setMeal(null);
        samplePlan.getEntries().add(entry);

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        List<ShoppingItem> result = mealPlanService.generateShoppingList(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void generateShoppingList_sortsByNameCaseInsensitive() {
        Meal meal = new Meal();
        meal.setIngredients(List.of(
                new Ingredient("Zucchini", 2, "pcs"),
                new Ingredient("Apple", 3, "pcs"),
                new Ingredient("banana", 1, "pcs")
        ));

        MealPlanEntry entry = new MealPlanEntry();
        entry.setMeal(meal);
        samplePlan.getEntries().add(entry);

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        List<ShoppingItem> result = mealPlanService.generateShoppingList(1L);

        assertThat(result).extracting(ShoppingItem::getName)
                .containsExactly("Apple", "banana", "Zucchini");
    }

    @Test
    void updateDayNotes_updatesNotes() {
        Map<String, String> notes = new HashMap<>();
        notes.put("MONDAY", "Takeaway night");
        notes.put("FRIDAY", "Date night");

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(samplePlan);

        mealPlanService.updateDayNotes(1L, notes);

        assertThat(samplePlan.getDayNotes()).containsEntry("MONDAY", "Takeaway night");
        assertThat(samplePlan.getDayNotes()).containsEntry("FRIDAY", "Date night");
    }

    @Test
    void updateDayNotes_handlesNullNotes() {
        samplePlan.getDayNotes().put("MONDAY", "Old note");
        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(samplePlan);

        mealPlanService.updateDayNotes(1L, null);

        // dayNotes should remain unchanged when null is passed
        assertThat(samplePlan.getDayNotes()).containsEntry("MONDAY", "Old note");
    }

    @Test
    void generateShoppingList_combinesSingularAndPluralIngredients() {
        Meal meal1 = new Meal();
        meal1.setIngredients(List.of(new Ingredient("Tomato", 2, "pcs")));

        Meal meal2 = new Meal();
        meal2.setIngredients(List.of(new Ingredient("Tomatoes", 3, "pcs")));

        MealPlanEntry entry1 = new MealPlanEntry();
        entry1.setMeal(meal1);
        MealPlanEntry entry2 = new MealPlanEntry();
        entry2.setMeal(meal2);
        samplePlan.getEntries().addAll(List.of(entry1, entry2));

        when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        List<ShoppingItem> result = mealPlanService.generateShoppingList(1L);

        // "Tomato" and "Tomatoes" should be combined
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalQuantity()).isEqualTo(5.0);
    }
}
