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

import java.util.Date;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public abstract class CoreMessage {

    public static enum MessageType {
        UnknownMessageType, InitiateSessionRequest, KillSessionRequest, ServiceRequest,
        InitiateSessionResponse, KillSessionResponse, ServiceResponse, DataResponse
    }


    private String sessionId;
    private String description;
    private String choreography = "";
    private String guid = "";
    private Date timestamp;
    private MessageType messageType;

    public String getChoreography() {
        return choreography;
    }

    public void setChoreography(String choreography) {
        this.choreography = choreography;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    protected CoreMessage(String sessionId, MessageType messageType) {
        this.sessionId = sessionId;
        this.messageType = messageType;
        description = "Session Id: " + sessionId + " Type: " + messageType.name();
        updateTimestamp();
    }

    public void updateTimestamp() {
        timestamp = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoreMessage)) {
            return false;
        }

        CoreMessage that = (CoreMessage) o;

        if (!guid.equals(that.guid)) {
            return false;
        }
        if (messageType != that.messageType) {
            return false;
        }
        if (!sessionId.equals(that.sessionId)) {
            return false;
        }
        if (!timestamp.equals(that.timestamp)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + guid.hashCode();
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + messageType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CoreMessage");
        sb.append("{sessionId='").append(sessionId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", choreography='").append(choreography).append('\'');
        sb.append(", guid='").append(guid).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", messageType=").append(messageType);
        sb.append('}');
        return sb.toString();
    }
}
