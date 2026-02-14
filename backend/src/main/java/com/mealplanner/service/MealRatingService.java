package com.mealplanner.service;

import com.mealplanner.dto.RatingRequest;
import com.mealplanner.model.Meal;
import com.mealplanner.model.MealRating;
import com.mealplanner.model.Person;
import com.mealplanner.repository.MealRatingRepository;
import com.mealplanner.repository.MealRepository;
import com.mealplanner.repository.PersonRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MealRatingService {

    private final MealRatingRepository ratingRepository;
    private final MealRepository mealRepository;
    private final PersonRepository personRepository;

    public MealRatingService(MealRatingRepository ratingRepository,
                             MealRepository mealRepository,
                             PersonRepository personRepository) {
        this.ratingRepository = ratingRepository;
        this.mealRepository = mealRepository;
        this.personRepository = personRepository;
    }

    public List<MealRating> findByMeal(Long mealId) {
        List<MealRating> allRatings = ratingRepository.findByMealId(mealId);
        // Deduplicate: keep only the latest rating per person
        Map<Long, MealRating> latestByPerson = new LinkedHashMap<>();
        for (MealRating r : allRatings) {
            Long personId = r.getPerson().getId();
            MealRating existing = latestByPerson.get(personId);
            if (existing == null || (r.getRatedDate() != null && (existing.getRatedDate() == null || r.getRatedDate().isAfter(existing.getRatedDate())))) {
                latestByPerson.put(personId, r);
            }
        }
        return new ArrayList<>(latestByPerson.values());
    }

    public Double getAverageRating(Long mealId) {
        return ratingRepository.findAverageRatingByMealId(mealId);
    }

    public MealRating create(@NonNull RatingRequest request) {
        Meal meal = mealRepository.findById(Objects.requireNonNull(request.getMealId()))
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        Person person = personRepository.findById(Objects.requireNonNull(request.getPersonId()))
                .orElseThrow(() -> new RuntimeException("Person not found"));

        // Upsert: one rating per person per meal
        MealRating rating = ratingRepository.findFirstByMealIdAndPersonId(
                request.getMealId(), request.getPersonId())
                .orElseGet(() -> {
                    MealRating r = new MealRating();
                    r.setMeal(meal);
                    r.setPerson(person);
                    return r;
                });

        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setRatedDate(LocalDate.now());

        return ratingRepository.save(rating);
    }

    public MealRating update(@NonNull Long id, RatingRequest request) {
        MealRating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        return ratingRepository.save(rating);
    }

    public void delete(@NonNull Long id) {
        ratingRepository.deleteById(id);
    }
}
