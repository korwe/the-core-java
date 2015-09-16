/*
 * Copyright (c) 2010.  Korwe Software
 *
 *  This file is part of TheCore.
 *
 *  TheCore is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TheCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with TheCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.korwe.thecore.messages;

import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static junit.framework.Assert.assertTrue;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreMessageXmlSerializerTest {

    public static final String SERVICE =
            "<coreMessage><sessionId>testSession</sessionId><messageType>ServiceRequest</messageType>" +
            "<guid>00000000-0000-0000-0000-000000000000</guid><choreography></choreography>" +
            "<description>Session Id: testSession Type: ServiceRequest</description><timeStamp>20100120T135009.123000</timeStamp>" +
            "<function>testMe</function><parameters><parameter><name>param1</name><value>value1</value></parameter>" +
            "<parameter><name>param2</name><value>value2</value>" +
            "</parameter><parameter><name>param3</name><value>value3</value>" +
            "</parameter></parameters></coreMessage>";

    public static final String SERVICE_TS_EXACT =
            "<coreMessage><sessionId>testSession</sessionId><messageType>ServiceRequest</messageType>" +
            "<guid>00000000-0000-0000-0000-000000000000</guid><choreography></choreography>" +
            "<description>Session Id: testSession Type: ServiceRequest</description><timeStamp>20100120T135009</timeStamp>" +
            "<function>testMe</function><parameters><parameter><name>param1</name><value>value1</value></parameter>" +
            "<parameter><name>param2</name><value>value2</value>" +
            "</parameter><parameter><name>param3</name><value>value3</value>" +
            "</parameter></parameters></coreMessage>";

    public static final String SERVICE_TS_EVIL =
            "<coreMessage><sessionId>testSession</sessionId><messageType>ServiceRequest</messageType>" +
            "<guid>00000000-0000-0000-0000-000000000000</guid><choreography></choreography>" +
            "<description>Session Id: testSession Type: ServiceRequest</description><timeStamp>20100120T135009.12</timeStamp>" +
            "<function>testMe</function><parameters><parameter><name>param1</name><value>value1</value></parameter>" +
            "<parameter><name>param2</name><value>value2</value>" +
            "</parameter><parameter><name>param3</name><value>value3</value>" +
            "</parameter></parameters></coreMessage>";

    @Test
    public void testSerialize() throws Exception {
        CoreMessageSerializer ser = new CoreMessageXmlSerializer();
        ServiceRequest message = new ServiceRequest("testSession", "testMe");
        message.setParameter("param1", "value1");
        message.setParameter("param2", "value2");
        message.setParameter("param3", "value3");
        message.setGuid("00000000-0000-0000-0000-000000000000");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        message.setTimestamp(df.parse("2010-01-20 11:50:09.123"));
        String actual = ser.serialize(message);
        String expected = SERVICE;
        assertTrue(actual.contains("<guid>00000000-0000-0000-0000-000000000000</guid>"));
        assertTrue(actual.contains("<parameter><name>param1</name><value>value1</value></parameter>"));
    }

    @Test
    public void testDeserializeExactTimestamp() throws Exception {
        CoreMessageSerializer ser = new CoreMessageXmlSerializer();
        CoreMessage msg = ser.deserialize(SERVICE_TS_EXACT);
        Assert.assertEquals("ServiceRequest", msg.getMessageType().name());
        Assert.assertEquals("value1", ((ServiceRequest) msg).getParameterValue("param1"));
    }

    @Test
    public void testDeserializeEvilTimestamp() throws Exception {
        CoreMessageSerializer ser = new CoreMessageXmlSerializer();
        CoreMessage msg = ser.deserialize(SERVICE_TS_EVIL);
        Assert.assertEquals("ServiceRequest", msg.getMessageType().name());
        Assert.assertEquals("value1", ((ServiceRequest) msg).getParameterValue("param1"));
    }

    @Test
    public void testDeserialize() throws Exception {
        CoreMessageSerializer ser = new CoreMessageXmlSerializer();
        CoreMessage msg = ser.deserialize(SERVICE);
        Assert.assertEquals("ServiceRequest", msg.getMessageType().name());
        Assert.assertEquals("value1", ((ServiceRequest) msg).getParameterValue("param1"));
    }
}
