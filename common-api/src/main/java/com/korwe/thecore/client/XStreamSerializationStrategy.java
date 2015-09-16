package com.korwe.thecore.client;

import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class XStreamSerializationStrategy implements SerializationStrategy {

    private XStream xStream;

    public XStreamSerializationStrategy(XStream xStream) {
        this.xStream = xStream;
    }

    @Override
    public String serialize(Object object) {
        return object == null ? null : xStream.toXML(object);
    }

    @Override
    public Object deserialize(String serializedObject) {
        return serializedObject == null ? null : xStream.fromXML(serializedObject);
    }
}
