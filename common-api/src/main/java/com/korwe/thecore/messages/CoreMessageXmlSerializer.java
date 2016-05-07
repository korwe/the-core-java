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

import com.jamesmurty.utils.XMLBuilder;
import com.korwe.thecore.exception.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreMessageXmlSerializer implements CoreMessageSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(CoreMessageXmlSerializer.class);

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd'T'HHmmss.SSS";

    @Override
    public String serialize(CoreMessage message) {
        try {
            DateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
            XMLBuilder builder = XMLBuilder.create("coreMessage").elem("sessionId").text(message.getSessionId())
                    .up().elem("messageType").text(message.getMessageType().name())
                    .up().elem("guid").text(message.getGuid())
                    .up().elem("choreography").text(message.getChoreography())
                    .up().elem("description").text(message.getDescription())
                    .up().elem("timeStamp").text(format.format(message.getTimestamp()));
            switch (message.getMessageType()) {
                case ServiceRequest:
                    ServiceRequest sreq = (ServiceRequest) message;
                    builder = builder.up().elem("function").text(sreq.getFunction());
                    if (sreq.getLocation() != null) {
                        builder = builder.up().elem("location").text(sreq.getLocation());
                    }
                    builder = builder.up().elem("parameters");
                    Iterator<String> parameterNames = sreq.getParameterNames();
                    while (parameterNames.hasNext()) {
                        String name = parameterNames.next();
                        if (sreq.getParameterValue(name) != null) {
                            builder.elem("parameter").elem("name").text(name).up().elem("value")
                                    .text(sreq.getParameterValue(name)).up();
                        }
                    }
                    builder = builder.up();
                    break;
                case InitiateSessionResponse:
                case KillSessionResponse:
                    builder = addResponseElements(builder, (CoreResponse) message);
                    break;
                case ServiceResponse:
                    ServiceResponse sres = (ServiceResponse) message;
                    builder = addResponseElements(builder, sres);
                    builder = builder.elem("hasData").t(sres.hasData() ? "1" : "0");
                    break;
                case DataResponse:
                    DataResponse dres = (DataResponse) message;
                    builder = addResponseElements(builder, dres);
                    builder = builder.elem("data").t(dres.getData());
                    break;
                default:
                    break;
            }
            Properties props = new Properties();
            props.put(OutputKeys.METHOD, "xml");
            props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            return builder.asString(props);
        }
        catch (ParserConfigurationException e) {
            LOG.error("XML parser error", e);
            return "";
        }
        catch (TransformerException e) {
            LOG.error("XML transformer error", e);
            return "";
        }

    }

    private XMLBuilder addResponseElements(XMLBuilder builder, CoreResponse response) {
        if(response.getErrorType()!=null){
            builder = builder.up().elem("errorType").text(String.valueOf(response.getErrorType().getErrorCode()));
        }

        if(response.getErrorVars()!=null && response.getErrorVars().size() >0){
            builder = builder.up().elem("errorVars");
            for(String var : response.getErrorVars()){
                builder = builder.elem("errorVar").text(var).up();
            }
            builder = builder.up();
        }

        builder = builder.up().elem("errorCode").text(response.getErrorCode()).up()
                .elem("errorMessage").text(response.getErrorMessage()).up()
                .elem("successful").text(response.isSuccessful() ? "1" : "0").up();
        return builder;
    }

    @Override
    public CoreMessage deserialize(String message) {
        try {
            DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docFac.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            return deserialize(doc);
        }
        catch (ParserConfigurationException e) {
            LOG.error("XML parser error", e);
            return null;
        }
        catch (SAXException e) {
            LOG.error("XML parser error", e);
            return null;
        }
        catch (IOException e) {
            LOG.error("XML parser error", e);
            return null;
        }
        catch (ParseException e) {
            LOG.error("Timestamp cannot be parsed", e);
            return null;
        }
    }

    public CoreMessage deserialize(Document doc) throws ParseException {
        Node root = doc.getFirstChild();
        if (root == null || !"coreMessage".equals(root.getNodeName())) {
            LOG.error("Message is not a CoreMessage");
            return null;
        }
        String sessionId = getTagValue(doc, "sessionId");
        if (sessionId == null) {
            return null;
        }

        String messageType = getTagValue(doc, "messageType");
        if (messageType == null) {
            return null;
        }

        String guid = getTagValue(doc, "guid");
        if (guid == null) {
            return null;
        }

        CoreMessage created = null;

        switch (CoreMessage.MessageType.valueOf(messageType)) {
            case InitiateSessionRequest:
                created = new InitiateSessionRequest(sessionId);
                created.setGuid(guid);
                break;
            case KillSessionRequest:
                created = new KillSessionRequest(sessionId);
                created.setGuid(guid);
                break;
            case ServiceRequest:
                String function = getTagValue(doc, "function");
                if (function != null) {
                    ServiceRequest sreq = new ServiceRequest(sessionId, function);
                    NodeList params = doc.getElementsByTagName("parameter");
                    for (int i = 0; i < params.getLength(); i++) {
                        Node param = params.item(i);
                        NodeList paramNodes = param.getChildNodes();
                        String pName = "";
                        String pValue = "";
                        for (int j = 0; j < paramNodes.getLength(); j++) {
                            Node node = paramNodes.item(j);
                            if ("name".equals(node.getNodeName())) {
                                pName = node.getTextContent();
                            }
                            else if ("value".equals(node.getNodeName())) {
                                pValue = node.getTextContent();
                            }
                        }
                        if (!pName.isEmpty()) {
                            sreq.setParameter(pName, pValue);
                        }
                    }
                    String location = getTagValue(doc, "location");
                    if (location != null) {
                        sreq.setLocation(location);
                    }
                    created = sreq;
                    created.setGuid(guid);
                }
                break;
            case InitiateSessionResponse:
                String issuccessValue = getTagValue(doc, "successful");
                CoreResponse isresp = new InitiateSessionResponse(sessionId, guid, "1".equals(issuccessValue));
                setResponseFields(isresp, doc);
                created = isresp;
                break;
            case KillSessionResponse:
                String kssuccessValue = getTagValue(doc, "successful");
                CoreResponse ksresp = new KillSessionResponse(sessionId, guid, "1".equals(kssuccessValue));
                setResponseFields(ksresp, doc);
                created = ksresp;
                break;
            case ServiceResponse:
                String hasDataValue = getTagValue(doc, "hasData");
                String successValue = getTagValue(doc, "successful");
                CoreResponse sresp =
                        new ServiceResponse(sessionId, guid, "1".equals(successValue), "1".equals(hasDataValue));
                setResponseFields(sresp, doc);
                created = sresp;
                break;
            case DataResponse:
                String data = getTagValue(doc, "data");
                CoreResponse dresp = new DataResponse(sessionId, guid, data);
                setResponseFields(dresp, doc);
                created = dresp;
                break;
            default:
                LOG.error("Unknown message type");
                break;
        }

        if (created != null) {
            created.setChoreography(getTagValue(doc, "choreography"));
            created.setDescription(getTagValue(doc, "description"));
            String tsValue = getTagValue(doc, "timeStamp");
            tsValue = normaliseTimestamp(tsValue);
            DateFormat df = new SimpleDateFormat(TIMESTAMP_FORMAT);
            created.setTimestamp(df.parse(tsValue));
        }

        return created;
    }

    private String normaliseTimestamp(String tsValue) {
        // Do an evil dance to compensate for timestamps sometimes having 0 or more than 3 fractional second digits
        int dotPosition = tsValue.indexOf(".");
        if (dotPosition > 0) {
            if (dotPosition + 3 >= tsValue.length()) {
                tsValue = tsValue.substring(0, dotPosition) + ".000";
            }
            else {
                tsValue = tsValue.substring(0, dotPosition + 3);
            }
        }
        else {
            tsValue = tsValue + ".000";
        }
        return tsValue;
    }

    private void setResponseFields(CoreResponse response, Document doc) {
        response.setErrorCode(getTagValue(doc, "errorCode"));
        response.setErrorMessage(getTagValue(doc, "errorMessage"));
        String errorType = getTagValue(doc, "errorType");
        if(!"".equals(errorType)){
            response.setErrorType(ErrorType.fromErrorCode(Integer.valueOf(errorType)));
        }

        NodeList errorVars = doc.getElementsByTagName("errorVar");
        for (int i = 0; i < errorVars.getLength(); i++) {
            response.getErrorVars().add(errorVars.item(i).getTextContent());
        }

    }

    private String getTagValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (0 == nodes.getLength() || !tagName.equals(nodes.item(0).getNodeName()) ||
            null == nodes.item(0).getTextContent() || nodes.item(0).getTextContent().isEmpty()) {
            LOG.debug("Message has no " + tagName);
            return "";
        }
        return nodes.item(0).getTextContent();
    }
}
