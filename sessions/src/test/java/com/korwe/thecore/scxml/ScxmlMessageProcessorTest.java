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

import com.korwe.thecore.api.CoreMessageHandler;
import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ScxmlMessageProcessorTest {

    @Test
    public void testInitialize() throws Exception {
        try {
            ScxmlMessageProcessor proc = new ScxmlMessageProcessor();
            proc.initialize("testsession1");
        }
        catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testProcessMessage() throws Exception {
        final ScxmlMessageProcessor proc = new ScxmlMessageProcessor();
        proc.initialize("testsession2");
        final ServiceRequest req = new ServiceRequest("testsession2", "login");
        req.setChoreography("LoginService");
        req.setParameter("username", "nithia");
        req.setParameter("password", "passwd");
        CoreSubscriber sub = new CoreSubscriber(MessageQueue.CoreToService, "SingleSignonService");
        final ServiceRequest[] singleSignon = new ServiceRequest[1];
        sub.connect(new CoreMessageHandler() {
            @Override
            public void handleMessage(CoreMessage message) {
                singleSignon[0] = (ServiceRequest) message;
                ServiceResponse resp = new ServiceResponse("testsession2", message.getGuid(), true, false);
                proc.processMessage(resp);
            }
        });
        proc.processMessage(req);
        Thread.sleep(2000);
        Assert.assertNotNull(singleSignon[0]);
        boolean machineFinished = proc.isStateMachineFinished();
        Assert.assertTrue(machineFinished);
        sub.close();
    }
}
