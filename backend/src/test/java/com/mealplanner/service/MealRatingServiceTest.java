package com.mealplanner.service;

import com.mealplanner.dto.RatingRequest;
import com.mealplanner.model.Meal;
import com.mealplanner.model.MealRating;
import com.mealplanner.model.Person;
import com.mealplanner.repository.MealRatingRepository;
import com.mealplanner.repository.MealRepository;
import com.mealplanner.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealRatingServiceTest {

    @Mock
    private MealRatingRepository ratingRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private MealRatingService ratingService;

    private Meal meal;
    private Person person1;
    private Person person2;

    @BeforeEach
    void setUp() {
        meal = new Meal();
        meal.setId(1L);
        meal.setTitle("Test Meal");

        person1 = new Person();
        person1.setId(1L);
        person1.setName("Alice");

        person2 = new Person();
        person2.setId(2L);
        person2.setName("Bob");
    }

    @Test
    void findByMeal_deduplicatesRatingsPerPerson() {
        MealRating olderRating = new MealRating();
        olderRating.setId(1L);
        olderRating.setPerson(person1);
        olderRating.setRating(3);
        olderRating.setRatedDate(LocalDate.of(2025, 1, 1));

        MealRating newerRating = new MealRating();
        newerRating.setId(2L);
        newerRating.setPerson(person1);
        newerRating.setRating(5);
        newerRating.setRatedDate(LocalDate.of(2025, 6, 1));

        MealRating otherPersonRating = new MealRating();
        otherPersonRating.setId(3L);
        otherPersonRating.setPerson(person2);
        otherPersonRating.setRating(4);
        otherPersonRating.setRatedDate(LocalDate.of(2025, 3, 1));

        when(ratingRepository.findByMealId(1L))
                .thenReturn(List.of(olderRating, newerRating, otherPersonRating));

        List<MealRating> result = ratingService.findByMeal(1L);

        assertThat(result).hasSize(2);
        // Person1's latest rating should be kept (rating=5)
        MealRating person1Rating = result.stream()
                .filter(r -> r.getPerson().getId().equals(1L))
                .findFirst().orElseThrow();
        assertThat(person1Rating.getRating()).isEqualTo(5);
    }

    @Test
    void findByMeal_returnsEmptyListWhenNoRatings() {
        when(ratingRepository.findByMealId(1L)).thenReturn(List.of());

        List<MealRating> result = ratingService.findByMeal(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getAverageRating_returnsAverage() {
        when(ratingRepository.findAverageRatingByMealId(1L)).thenReturn(4.5);

        Double result = ratingService.getAverageRating(1L);

        assertThat(result).isEqualTo(4.5);
    }

    @Test
    void getAverageRating_returnsNullWhenNoRatings() {
        when(ratingRepository.findAverageRatingByMealId(1L)).thenReturn(null);

        Double result = ratingService.getAverageRating(1L);

        assertThat(result).isNull();
    }

    @Test
    void create_createsNewRating() {
        RatingRequest request = new RatingRequest();
        request.setMealId(1L);
        request.setPersonId(1L);
        request.setRating(4);
        request.setComment("Delicious!");

        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(1L)).thenReturn(Optional.of(person1));
        when(ratingRepository.findFirstByMealIdAndPersonId(1L, 1L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(MealRating.class))).thenAnswer(inv -> inv.getArgument(0));

        MealRating result = ratingService.create(request);

        assertThat(result.getMeal()).isEqualTo(meal);
        assertThat(result.getPerson()).isEqualTo(person1);
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Delicious!");
        assertThat(result.getRatedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void create_updatesExistingRatingForSamePersonAndMeal() {
        MealRating existingRating = new MealRating();
        existingRating.setId(1L);
        existingRating.setMeal(meal);
        existingRating.setPerson(person1);
        existingRating.setRating(3);

        RatingRequest request = new RatingRequest();
        request.setMealId(1L);
        request.setPersonId(1L);
        request.setRating(5);
        request.setComment("Even better this time!");

        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(1L)).thenReturn(Optional.of(person1));
        when(ratingRepository.findFirstByMealIdAndPersonId(1L, 1L))
                .thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(any(MealRating.class))).thenAnswer(inv -> inv.getArgument(0));

        MealRating result = ratingService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Even better this time!");
    }

    @Test
    void create_throwsWhenMealNotFound() {
        RatingRequest request = new RatingRequest();
        request.setMealId(99L);
        request.setPersonId(1L);

        when(mealRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meal not found");
    }

    @Test
    void create_throwsWhenPersonNotFound() {
        RatingRequest request = new RatingRequest();
        request.setMealId(1L);
        request.setPersonId(99L);

        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Person not found");
    }

    @Test
    void update_updatesExistingRating() {
        MealRating existing = new MealRating();
        existing.setId(1L);
        existing.setRating(3);
        existing.setComment("OK");

        RatingRequest request = new RatingRequest();
        request.setRating(5);
        request.setComment("Great!");

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(MealRating.class))).thenAnswer(inv -> inv.getArgument(0));

        MealRating result = ratingService.update(1L, request);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great!");
    }

    @Test
    void update_throwsWhenRatingNotFound() {
        when(ratingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.update(99L, new RatingRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rating not found");
    }

    @Test
    void delete_deletesRatingById() {
        ratingService.delete(1L);

        verify(ratingRepository).deleteById(1L);
    }
}
