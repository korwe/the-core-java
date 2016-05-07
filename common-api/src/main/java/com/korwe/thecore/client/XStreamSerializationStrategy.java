package com.korwe.thecore.client;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class XStreamSerializationStrategy implements ParamSerializationStrategy {

    private XStream xStream;
    private Logger log = LoggerFactory.getLogger(XStreamSerializationStrategy.class);
    private XStream xStreamDefault = new XStream();
    public XStreamSerializationStrategy(XStream xStream) {
        this.xStream = xStream;
    }

    @Override
    public String serialize(Object object) {
        return object == null ? null : xStream.toXML(object);
    }

    @Override
    public Object deserialize(String serializedObject) {
        try{
            return serializedObject == null ? null : xStream.fromXML(serializedObject);
        }
        catch (Exception e){
            log.error("Xstream message deserialization failed with provided xstream handler: {}", serializedObject, e);
        }

        try{
            log.info("Trying default xstream");

            return xStreamDefault.fromXML(serializedObject);
        }
        catch (Exception e){
            log.error("Default xstream deserialer failed");
        }

        return null;


    }
}
