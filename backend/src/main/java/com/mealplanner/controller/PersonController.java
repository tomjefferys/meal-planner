package com.mealplanner.controller;

import com.mealplanner.model.Person;
import com.mealplanner.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public List<Person> getAll() {
        return personService.findAll();
    }

    @GetMapping("/{id}")
    public Person getById(@PathVariable @NonNull Long id) {
        return personService.findById(id);
    }

    @PostMapping
    public Person create(@RequestBody @NonNull Person person) {
        return personService.create(person);
    }

    @PutMapping("/{id}")
    public Person update(@PathVariable @NonNull Long id, @RequestBody Person person) {
        return personService.update(id, person);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
