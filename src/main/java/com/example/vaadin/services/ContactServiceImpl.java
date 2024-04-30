package com.example.vaadin.services;

import com.example.vaadin.entities.Contact;
import com.example.vaadin.entities.Person;
import com.example.vaadin.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    @Autowired
    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    @Override
    public void save(String name, Long phoneNumber, Person person) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setPerson(person);
        contactRepository.save(contact);
    }

    @Transactional
    @Override
    public void delete(Contact contact) {
        contactRepository.delete(contact);
    }
}
