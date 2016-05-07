/*
 * Copyright (c) 2011.  Korwe Software
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

package com.korwe.thecore.service;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.korwe.thecore.api.CoreFactory;
import com.korwe.thecore.api.CoreFactoryImpl;
import com.korwe.thecore.messages.CoreMessageXmlSerializer;
import com.korwe.thecore.service.ping.CorePingService;
import com.korwe.thecore.service.syndication.CoreSyndicationService;
import com.korwe.thecore.service.syndication.SyndicationServiceImpl;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreServices {

    private static final Logger LOG = LoggerFactory.getLogger(CoreServices.class);

    private static Set<AbstractCoreService> services = new HashSet<>(5);

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        CoreFactory coreFactory = new CoreFactoryImpl(connectionFactory, new CoreMessageXmlSerializer());
        services.add(new CorePingService(10,coreFactory));
        services.add(new CoreSyndicationService(new SyndicationServiceImpl(), 10, coreFactory));

        ServiceManager manager = new ServiceManager(services);
        manager.addListener(new ServiceManager.Listener() {
            @Override
            public void healthy() {
                LOG.info("All services started & healthy");
            }

            @Override
            public void stopped() {
                LOG.info("All services stopped");
            }

            @Override
            public void failure(final Service service) {
                LOG.info("Service {} failed", service);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
                }
                catch (TimeoutException timeout) {
                    LOG.error("Stopping services timed out");
                }
            }
        });

        manager.startAsync().awaitHealthy();
        // TODO check from time to time if all is still ok
        manager.awaitStopped();
    }
}
