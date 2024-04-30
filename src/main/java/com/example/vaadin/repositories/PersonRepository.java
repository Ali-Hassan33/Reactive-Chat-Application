package com.example.vaadin.repositories;

import com.example.vaadin.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    Optional<Person> findByPhoneNumber(Long phoneNumber);

    Optional<Person> findByName(String username);

    boolean existsByName(String username);

    boolean existsByPhoneNumber(Long phoneNumber);
}
