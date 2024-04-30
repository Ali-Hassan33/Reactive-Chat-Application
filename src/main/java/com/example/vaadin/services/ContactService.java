package com.example.vaadin.services;

import com.example.vaadin.entities.Contact;
import com.example.vaadin.entities.Person;

public interface ContactService {

    void save(String name, Long phoneNumber, Person person);

    void delete(Contact contact);
}
