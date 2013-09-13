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

import com.korwe.thecore.api.CoreConfig;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.session.BasicMessageProcessor;
import com.korwe.thecore.session.CoreMessageProcessor;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.*;
import org.apache.commons.scxml.env.jexl.JexlContext;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ScxmlMessageProcessor extends BasicMessageProcessor implements CoreMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ScxmlMessageProcessor.class);

    private SCXML scxml;
    private SCXMLExecutor exec;

    @Override
    public void initialize(String sessionId) {
        super.initialize(sessionId);
        try {
            String scxmlPath = CoreConfig.getInstance().getProperty("scxml_path");
            if (LOG.isDebugEnabled()) {
                LOG.debug("SCXML path = " + scxmlPath);
            }
            File scfile = new File(scxmlPath);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Absolute path: [" + scfile.getAbsolutePath() + "]");
            }

            InputSource source = new InputSource(new BufferedReader(new FileReader(scxmlPath)));

            scxml = SCXMLParser.parse(source, new SimpleErrorHandler());
            exec = new SCXMLExecutor(new JexlEvaluator(), new SimpleDispatcher(), new SimpleErrorReporter());
            exec.setStateMachine(scxml);
            exec.addListener(scxml, new SimpleSCXMLListener());
            exec.registerInvokerClass("x-coremessage", SendCoreMessageInvoker.class);

            Context context = new JexlContext();
            context.set("sessionId", sessionId);
            context.set("lastMsg", null);
            exec.setRootContext(context);

            exec.go();

        }
        catch (Exception e) {
            LOG.error("Failed to parse SCXML", e);
        }
    }

    @Override
    public synchronized void processMessage(CoreMessage message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message = " + message);
            LOG.debug("State machine state = " + exec.getCurrentStatus().getStates());
        }

        try {
            switch (message.getMessageType()) {
                case ServiceRequest:
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Triggering request event");
                    }

                    ServiceRequest req = (ServiceRequest) message;
                    exec.triggerEvent(new TriggerEvent("ServiceRequest." + req.getFunction(),
                                                       TriggerEvent.SIGNAL_EVENT, req));
                    break;
                default:
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Triggering default event");
                    }

                    exec.triggerEvent(new TriggerEvent(message.getMessageType().name(),
                                                       TriggerEvent.SIGNAL_EVENT, message));
                    break;
            }
        }
        catch (ModelException e) {
            LOG.error("Unable to process message - state machine error", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Message processed");
        }

    }

    public boolean isStateMachineFinished() {
        return exec.getCurrentStatus().isFinal();
    }
}
