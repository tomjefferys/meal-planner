package com.mealplanner.service;

import com.mealplanner.dto.EntryRequest;
import com.mealplanner.dto.ShoppingItem;
import com.mealplanner.model.*;
import com.mealplanner.repository.MealPlanEntryRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.MealRepository;
import com.mealplanner.repository.PersonRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.Objects;

@Service
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanEntryRepository entryRepository;
    private final MealRepository mealRepository;
    private final PersonRepository personRepository;

    public MealPlanService(MealPlanRepository mealPlanRepository,
                           MealPlanEntryRepository entryRepository,
                           MealRepository mealRepository,
                           PersonRepository personRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.entryRepository = entryRepository;
        this.mealRepository = mealRepository;
        this.personRepository = personRepository;
    }

    public List<MealPlan> findAll() {
        return mealPlanRepository.findAllByOrderByWeekStartDateDesc();
    }

    public MealPlan findById(@NonNull Long id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal plan not found with id: " + id));
    }

    @Transactional
    public MealPlan getOrCreateForWeek(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        return mealPlanRepository.findByWeekStartDate(weekStart)
                .orElseGet(() -> {
                    MealPlan plan = new MealPlan();
                    plan.setWeekStartDate(weekStart);
                    return mealPlanRepository.save(plan);
                });
    }

    @Transactional
    public MealPlanEntry addEntry(@NonNull Long planId, @NonNull EntryRequest request) {
        MealPlan plan = findById(planId);
        Meal meal = mealRepository.findById(Objects.requireNonNull(request.getMealId()))
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        MealPlanEntry entry = new MealPlanEntry();
        entry.setMealPlan(plan);
        entry.setMeal(meal);
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setMealType(MealType.valueOf(request.getMealType()));

        if (request.getAssignedCookId() != null) {
            Person cook = personRepository.findById(Objects.requireNonNull(request.getAssignedCookId()))
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            entry.setAssignedCook(cook);
        }

        plan.getEntries().add(entry);
        mealPlanRepository.save(plan);
        return entry;
    }

    @Transactional
    public MealPlanEntry updateEntry(@NonNull Long entryId, @NonNull EntryRequest request) {
        MealPlanEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (request.getDayOfWeek() != null) {
            entry.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getMealType() != null) {
            entry.setMealType(MealType.valueOf(request.getMealType()));
        }
        if (request.getMealId() != null) {
            Meal meal = mealRepository.findById(Objects.requireNonNull(request.getMealId()))
                    .orElseThrow(() -> new RuntimeException("Meal not found"));
            entry.setMeal(meal);
        }
        if (request.getAssignedCookId() != null) {
            Person cook = personRepository.findById(Objects.requireNonNull(request.getAssignedCookId()))
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            entry.setAssignedCook(cook);
        }

        return entryRepository.save(Objects.requireNonNull(entry));
    }

    @Transactional
    public void deleteEntry(@NonNull Long entryId) {
        MealPlanEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        MealPlan plan = entry.getMealPlan();
        plan.getEntries().remove(entry);
        mealPlanRepository.save(plan);
    }

    public List<ShoppingItem> generateShoppingList(@NonNull Long planId) {
        MealPlan plan = findById(planId);
        return combineIngredients(plan.getEntries());
    }

    public List<ShoppingItem> generateShoppingListForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate searchFrom = startDate.minusDays(6);
        List<MealPlan> plans = mealPlanRepository.findByWeekStartDateBetween(searchFrom, endDate);

        List<MealPlanEntry> entriesInRange = new ArrayList<>();
        for (MealPlan plan : plans) {
            for (MealPlanEntry entry : plan.getEntries()) {
                LocalDate entryDate = plan.getWeekStartDate().plusDays(dayOffset(entry.getDayOfWeek()));
                if (!entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)) {
                    entriesInRange.add(entry);
                }
            }
        }

        return combineIngredients(entriesInRange);
    }

    private List<ShoppingItem> combineIngredients(List<MealPlanEntry> entries) {
        Map<String, ShoppingItem> items = new LinkedHashMap<>();

        for (MealPlanEntry entry : entries) {
            if (entry.getMeal() == null || entry.getMeal().getIngredients() == null) continue;

            for (Ingredient ingredient : entry.getMeal().getIngredients()) {
                String normalizedName = normalizeIngredientName(ingredient.getName());
                String normalizedUnit = normalizeIngredientName(ingredient.getUnit());
                String key = normalizedName + "|" + normalizedUnit;

                items.merge(key,
                        new ShoppingItem(ingredient.getName(), ingredient.getQuantity(), ingredient.getUnit()),
                        (existing, newItem) -> {
                            existing.setTotalQuantity(existing.getTotalQuantity() + newItem.getTotalQuantity());
                            return existing;
                        });
            }
        }

        List<ShoppingItem> result = new ArrayList<>(items.values());
        result.sort(Comparator.comparing(ShoppingItem::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private String normalizeIngredientName(String name) {
        if (name == null) return "";
        return singularize(name.toLowerCase().trim());
    }

    private String singularize(String word) {
        if (word == null || word.isEmpty()) return word;
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("sses") || word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("ves") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "f";
        }
        if (word.endsWith("oes") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us") && word.length() > 2) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private int dayOffset(String dayOfWeek) {
        return switch (dayOfWeek) {
            case "SATURDAY" -> 0;
            case "SUNDAY" -> 1;
            case "MONDAY" -> 2;
            case "TUESDAY" -> 3;
            case "WEDNESDAY" -> 4;
            case "THURSDAY" -> 5;
            case "FRIDAY" -> 6;
            default -> 0;
        };
    }

    @Transactional
    public MealPlan updateDayNotes(@NonNull Long planId, java.util.Map<String, String> dayNotes) {
        MealPlan plan = findById(planId);
        if (dayNotes != null) {
            plan.getDayNotes().clear();
            plan.getDayNotes().putAll(dayNotes);
        }
        return mealPlanRepository.save(Objects.requireNonNull(plan));
    }
}
