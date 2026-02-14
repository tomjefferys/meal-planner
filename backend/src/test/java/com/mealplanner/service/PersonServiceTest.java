package com.mealplanner.service;

import com.mealplanner.model.Person;
import com.mealplanner.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

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
    void findAll_returnsAllPeople() {
        when(personRepository.findAll()).thenReturn(List.of(samplePerson));

        List<Person> result = personService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mum");
    }

    @Test
    void findAll_returnsEmptyList() {
        when(personRepository.findAll()).thenReturn(Collections.emptyList());

        List<Person> result = personService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsPersonWhenFound() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(samplePerson));

        Person result = personService.findById(1L);

        assertThat(result.getName()).isEqualTo("Mum");
        assertThat(result.getEatingPreferences()).isEqualTo("Vegetarian");
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Person not found with id: 99");
    }

    @Test
    void create_savesPerson() {
        when(personRepository.save(any(Person.class))).thenReturn(samplePerson);

        Person result = personService.create(samplePerson);

        assertThat(result.getId()).isEqualTo(1L);
        verify(personRepository).save(samplePerson);
    }

    @Test
    void update_updatesExistingPerson() {
        Person updated = new Person();
        updated.setName("Dad");
        updated.setEatingPreferences("No restrictions");
        updated.setCookingPreferences("BBQ expert");

        when(personRepository.findById(1L)).thenReturn(Optional.of(samplePerson));
        when(personRepository.save(any(Person.class))).thenReturn(samplePerson);

        personService.update(1L, updated);

        assertThat(samplePerson.getName()).isEqualTo("Dad");
        assertThat(samplePerson.getEatingPreferences()).isEqualTo("No restrictions");
        assertThat(samplePerson.getCookingPreferences()).isEqualTo("BBQ expert");
        verify(personRepository).save(samplePerson);
    }

    @Test
    void update_throwsWhenPersonNotFound() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.update(99L, new Person()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Person not found");
    }

    @Test
    void delete_deletesPersonById() {
        personService.delete(1L);

        verify(personRepository).deleteById(1L);
    }
}
