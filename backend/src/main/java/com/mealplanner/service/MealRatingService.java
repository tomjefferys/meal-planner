package com.mealplanner.service;

import com.mealplanner.dto.RatingRequest;
import com.mealplanner.model.Meal;
import com.mealplanner.model.MealRating;
import com.mealplanner.model.Person;
import com.mealplanner.repository.MealRatingRepository;
import com.mealplanner.repository.MealRepository;
import com.mealplanner.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
        return ratingRepository.findByMealId(mealId);
    }

    public Double getAverageRating(Long mealId) {
        return ratingRepository.findAverageRatingByMealId(mealId);
    }

    public MealRating create(RatingRequest request) {
        Meal meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        Person person = personRepository.findById(request.getPersonId())
                .orElseThrow(() -> new RuntimeException("Person not found"));

        MealRating rating = new MealRating();
        rating.setMeal(meal);
        rating.setPerson(person);
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setRatedDate(LocalDate.now());

        return ratingRepository.save(rating);
    }

    public MealRating update(Long id, RatingRequest request) {
        MealRating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        return ratingRepository.save(rating);
    }

    public void delete(Long id) {
        ratingRepository.deleteById(id);
    }
}
