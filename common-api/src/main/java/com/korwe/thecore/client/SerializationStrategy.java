package com.korwe.thecore.client;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public interface SerializationStrategy {

    public String serialize(Object object);

    public Object deserialize(String serializedObject);
}
