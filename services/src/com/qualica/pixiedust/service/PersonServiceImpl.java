package com.qualica.pixiedust.service;

import com.qualica.pixiedust.domain.Person;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class PersonServiceImpl implements PersonService {

    private SessionFactory sessionFactory;

    public PersonServiceImpl() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    public Person savePerson(String firstName, String lastName, String phoneNumber) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPhoneNumber(phoneNumber);
        session.saveOrUpdate(person);
        session.getTransaction().commit();
        return person;
    }
}
