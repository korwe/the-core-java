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

import com.korwe.thecore.exception.ErrorType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CoreResponse extends CoreMessage {

    private boolean successful;
    private ErrorType errorType;
    private String errorCode = "";
    private String errorMessage = "";
    private List<String> errorVars = new ArrayList<String>();

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public List<String> getErrorVars() {
        return errorVars;
    }

    public void setErrorVars(List<String> errorVars) {
        this.errorVars = errorVars;
    }

    protected CoreResponse(String sessionId, MessageType messageType, String guid, boolean successful) {
        super(sessionId, messageType);
        this.successful = successful;
        setGuid(guid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CoreResponse{");
        sb.append(super.toString());
        if(errorType!=null){
            sb.append(" errorType=").append(errorType.getErrorCode());
        }
        sb.append(", errorCode='").append(errorCode).append('\'');
        sb.append(", successful=").append(successful);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        if (errorVars.size() > 0){
            sb.append(", errorVars='");
            Iterator<String> varsIterator = errorVars.iterator();
            String var;
            while (varsIterator.hasNext()){
                var = varsIterator.next();
                sb.append(var);
                if(varsIterator.hasNext()){
                    sb.append(", ");
                }
            }
            sb.append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
