package com.qualica.pixiedust.service;

import com.qualica.pixiedust.domain.Person;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public interface PersonService {

    Person savePerson(String firstName, String lastName, String phoneNumber);
}
