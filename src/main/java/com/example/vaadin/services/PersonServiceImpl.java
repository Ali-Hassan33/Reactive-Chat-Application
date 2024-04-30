package com.example.vaadin.services;

import com.example.vaadin.entities.Contact;
import com.example.vaadin.entities.Person;
import com.example.vaadin.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    private final ContactService contactService;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository, ContactService contactService) {
        this.personRepository = personRepository;
        this.contactService = contactService;
    }

    @Override
    public Person getByPhoneNumber(long phoneNo) {
        Optional<Person> personOptional = personRepository.findByPhoneNumber(phoneNo);
        return personOptional.orElse(null);
    }

    @Override
    public Person getByUsername(String username) {
        Optional<Person> personOptional = personRepository.findByName(username);
        return personOptional.orElse(null);
    }

    @Transactional
    @Override
    public void updateContacts(Person loggedInPerson, Person personToContact) {
        List<Contact> list = loggedInPerson.getContactsList();
        list.add(new Contact(personToContact.getName(), personToContact.getPhoneNumber(), loggedInPerson));
        contactService.save(personToContact.getName(), personToContact.getPhoneNumber(), loggedInPerson);
    }

    @Transactional
    public void save(Person person) {
        personRepository.save(person);
    }

    @Override
    public boolean containsName(String username) {
        return personRepository.existsByName(username);
    }

    @Override
    public boolean containsPhoneNumber(long phoneNumber) {
        return personRepository.existsByPhoneNumber(phoneNumber);
    }
}
