package com.mealplanner.service;

import com.mealplanner.model.Person;
import com.mealplanner.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Person findById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
    }

    public Person create(Person person) {
        return personRepository.save(person);
    }

    public Person update(Long id, Person updated) {
        Person person = findById(id);
        person.setName(updated.getName());
        person.setEatingPreferences(updated.getEatingPreferences());
        person.setCookingPreferences(updated.getCookingPreferences());
        return personRepository.save(person);
    }

    public void delete(Long id) {
        personRepository.deleteById(id);
    }
}
