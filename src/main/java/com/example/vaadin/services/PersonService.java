package com.example.vaadin.services;

import com.example.vaadin.entities.Person;

public interface PersonService {

    Person getByPhoneNumber(long phoneNumber);

    Person getByUsername(String username);

    void updateContacts(Person loggedInPerson, Person personToContact);

    void save(Person person);

    boolean containsName(String username);

    boolean containsPhoneNumber(long phoneNumber);
}
