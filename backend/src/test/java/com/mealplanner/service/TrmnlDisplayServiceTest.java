package com.mealplanner.service;

import com.mealplanner.model.*;
import com.mealplanner.repository.MealPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrmnlDisplayServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @InjectMocks
    private TrmnlDisplayService displayService;

    private MealPlan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new MealPlan();
        testPlan.setId(1L);
        // Week starting Saturday 2026-02-14
        testPlan.setWeekStartDate(LocalDate.of(2026, 2, 14));

        Meal dinner = new Meal();
        dinner.setId(1L);
        dinner.setTitle("Spaghetti Bolognese");

        Person cook = new Person();
        cook.setId(1L);
        cook.setName("Tom");

        MealPlanEntry entry = new MealPlanEntry();
        entry.setId(1L);
        entry.setMealPlan(testPlan);
        entry.setMeal(dinner);
        entry.setDayOfWeek("SATURDAY");
        entry.setMealType(MealType.DINNER);
        entry.setAssignedCook(cook);
        entry.setDisplayOrder(0);

        testPlan.setEntries(new ArrayList<>(List.of(entry)));
    }

    @Test
    void renderDisplayImage_producesValidPng() throws IOException {
        LocalDate today = LocalDate.of(2026, 2, 14); // Saturday
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));

        when(mealPlanRepository.findByWeekStartDate(weekStart))
                .thenReturn(Optional.of(testPlan));

        byte[] image = displayService.renderDisplayImage(today);

        assertNotNull(image);
        assertTrue(image.length > 0);

        // Verify it's a valid PNG
        BufferedImage parsed = ImageIO.read(new ByteArrayInputStream(image));
        assertNotNull(parsed);
        assertEquals(TrmnlDisplayService.WIDTH, parsed.getWidth());
        assertEquals(TrmnlDisplayService.HEIGHT, parsed.getHeight());
    }

    @Test
    void renderDisplayImage_handlesNoMealPlan() throws IOException {
        LocalDate today = LocalDate.of(2026, 2, 14);

        when(mealPlanRepository.findByWeekStartDate(any())).thenReturn(Optional.empty());

        byte[] image = displayService.renderDisplayImage(today);

        assertNotNull(image);
        assertTrue(image.length > 0);

        BufferedImage parsed = ImageIO.read(new ByteArrayInputStream(image));
        assertNotNull(parsed);
        assertEquals(TrmnlDisplayService.WIDTH, parsed.getWidth());
        assertEquals(TrmnlDisplayService.HEIGHT, parsed.getHeight());
    }

    @Test
    void getEntriesForDate_returnsMatchingEntries() {
        LocalDate saturday = LocalDate.of(2026, 2, 14);
        LocalDate weekStart = saturday.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));

        when(mealPlanRepository.findByWeekStartDate(weekStart))
                .thenReturn(Optional.of(testPlan));

        List<MealPlanEntry> entries = displayService.getEntriesForDate(saturday);

        assertEquals(1, entries.size());
        assertEquals("Spaghetti Bolognese", entries.get(0).getMeal().getTitle());
    }

    @Test
    void getEntriesForDate_returnsEmptyForDayWithNoEntries() {
        LocalDate sunday = LocalDate.of(2026, 2, 15);
        LocalDate weekStart = sunday.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));

        when(mealPlanRepository.findByWeekStartDate(weekStart))
                .thenReturn(Optional.of(testPlan));

        List<MealPlanEntry> entries = displayService.getEntriesForDate(sunday);

        assertTrue(entries.isEmpty());
    }

    @Test
    void getEntriesForDate_returnsEmptyWhenNoPlanExists() {
        LocalDate date = LocalDate.of(2026, 3, 1);

        when(mealPlanRepository.findByWeekStartDate(any())).thenReturn(Optional.empty());

        List<MealPlanEntry> entries = displayService.getEntriesForDate(date);

        assertTrue(entries.isEmpty());
    }

    @Test
    void renderDisplayImage_includesMultipleMealTypes() throws IOException {
        // Add breakfast and lunch entries
        Meal breakfast = new Meal();
        breakfast.setId(2L);
        breakfast.setTitle("Pancakes");

        MealPlanEntry breakfastEntry = new MealPlanEntry();
        breakfastEntry.setId(2L);
        breakfastEntry.setMealPlan(testPlan);
        breakfastEntry.setMeal(breakfast);
        breakfastEntry.setDayOfWeek("SATURDAY");
        breakfastEntry.setMealType(MealType.BREAKFAST);
        breakfastEntry.setDisplayOrder(0);

        Meal lunch = new Meal();
        lunch.setId(3L);
        lunch.setTitle("Caesar Salad");

        MealPlanEntry lunchEntry = new MealPlanEntry();
        lunchEntry.setId(3L);
        lunchEntry.setMealPlan(testPlan);
        lunchEntry.setMeal(lunch);
        lunchEntry.setDayOfWeek("SATURDAY");
        lunchEntry.setMealType(MealType.LUNCH);
        lunchEntry.setDisplayOrder(0);

        testPlan.getEntries().add(breakfastEntry);
        testPlan.getEntries().add(lunchEntry);

        LocalDate today = LocalDate.of(2026, 2, 14);
        when(mealPlanRepository.findByWeekStartDate(any()))
                .thenReturn(Optional.of(testPlan));

        byte[] image = displayService.renderDisplayImage(today);

        assertNotNull(image);
        BufferedImage parsed = ImageIO.read(new ByteArrayInputStream(image));
        assertNotNull(parsed);
        assertEquals(TrmnlDisplayService.WIDTH, parsed.getWidth());
    }

    @Test
    void renderDisplayImage_includesNotes() throws IOException {
        testPlan.getDayNotes().put("SATURDAY", "Don't forget the garlic bread!");

        LocalDate today = LocalDate.of(2026, 2, 14);
        when(mealPlanRepository.findByWeekStartDate(any()))
                .thenReturn(Optional.of(testPlan));

        byte[] image = displayService.renderDisplayImage(today);

        assertNotNull(image);
        assertTrue(image.length > 0);
    }

    @Test
    void renderDisplayImage_crossesWeekBoundary() throws IOException {
        // Friday is day 6 of the week (Sat-start), tomorrow (Saturday) starts a new week
        LocalDate friday = LocalDate.of(2026, 2, 20);
        LocalDate saturday = friday.plusDays(1); // New week starts

        MealPlan nextWeekPlan = new MealPlan();
        nextWeekPlan.setId(2L);
        nextWeekPlan.setWeekStartDate(saturday);
        nextWeekPlan.setEntries(new ArrayList<>());

        LocalDate fridayWeekStart = friday.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        LocalDate saturdayWeekStart = saturday.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));

        when(mealPlanRepository.findByWeekStartDate(fridayWeekStart))
                .thenReturn(Optional.of(testPlan));
        when(mealPlanRepository.findByWeekStartDate(saturdayWeekStart))
                .thenReturn(Optional.of(nextWeekPlan));

        byte[] image = displayService.renderDisplayImage(friday);

        assertNotNull(image);
        assertTrue(image.length > 0);
    }
}
