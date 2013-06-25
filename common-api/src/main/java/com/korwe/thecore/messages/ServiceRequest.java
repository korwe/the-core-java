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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ServiceRequest extends CoreRequest {

    private String location;
    private String function = "";
    private Map<String, String> params = new HashMap<String, String>(1);

    public ServiceRequest(String sessionId, String function) {
        super(sessionId, MessageType.ServiceRequest);
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public Iterator<String> getParameterNames() {
        return params.keySet().iterator();
    }

    public String getParameterValue(String name) {
        return params.containsKey(name) ? params.get(name) : "";
    }

    public void setParameter(String name, String value) {
        params.put(name, value);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("ServiceRequest");
        sb.append("{function='").append(function).append('\'');
        sb.append(", params=").append(params);
        sb.append(", location='").append(location).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
