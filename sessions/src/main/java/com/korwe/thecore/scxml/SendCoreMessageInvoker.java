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

package com.korwe.thecore.scxml;

import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.invoke.InvokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class SendCoreMessageInvoker implements Invoker {

    private static final Logger LOG = LoggerFactory.getLogger(SendCoreMessageInvoker.class);

    private String parentStateId;
    private SCInstance scInstance;
    private TriggerEvent[] parentEvents;

    @Override
    public void setParentStateId(String parentStateId) {
        this.parentStateId = parentStateId;
    }

    @Override
    public void setSCInstance(SCInstance scInstance) {
        this.scInstance = scInstance;
    }

    @Override
    public void invoke(String source, Map params) throws InvokerException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Invoked with source = " + source + " and params = " + params);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting message sender");
        }

        new Thread(new ScxmlMessageSender(source, params, scInstance, parentStateId)).start();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message sender started");
        }

    }

    @Override
    public void parentEvents(TriggerEvent[] evts) throws InvokerException {
        parentEvents = evts;
    }

    @Override
    public void cancel() throws InvokerException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Message sending canceled, but I can't stop it now");
        }
    }
}
