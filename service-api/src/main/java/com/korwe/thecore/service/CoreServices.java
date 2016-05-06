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
import com.korwe.thecore.service.ping.CorePingService;
import com.korwe.thecore.service.syndication.CoreSyndicationService;
import com.korwe.thecore.service.syndication.SyndicationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreServices {

    private static final Logger LOG = LoggerFactory.getLogger(CoreServices.class);

    private static Set<AbstractCoreService> services = new HashSet<>(5);

    public static void main(String[] args) {
        services.add(new CorePingService(10));
        services.add(new CoreSyndicationService(new SyndicationServiceImpl(),10));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                services.forEach(Service::stop);
            }
        });

        CountDownLatch serviceLatch = new CountDownLatch(services.size());

        for (AbstractCoreService service : services) {
            service.addListener(new ServiceListener(serviceLatch, service.getServiceName()), MoreExecutors.sameThreadExecutor());
            service.start();
        }

        while (serviceLatch.getCount() > 0) {
            try {
                serviceLatch.await();
            }
            catch (InterruptedException e) {
                LOG.info("Await interrupted, retrying");
            }
        }
    }

    private static class ServiceListener implements Service.Listener {

        private final CountDownLatch serviceLatch;
        private final String serviceName;

        ServiceListener(final CountDownLatch serviceLatch, final String serviceName) {
            this.serviceLatch = serviceLatch;
            this.serviceName = serviceName;
        }

        @Override
        public void starting() {
            LOG.info("Service {} starting", serviceName);
        }

        @Override
        public void running() {
            LOG.info("Service {} running", serviceName);
        }

        @Override
        public void stopping(final Service.State from) {
            LOG.info("Service {} stopping", serviceName);
        }

        @Override
        public void terminated(final Service.State from) {
            serviceLatch.countDown();
            LOG.info("Service {} stopped, latch counted down: {}", serviceName, serviceLatch.getCount());
        }

        @Override
        public void failed(final Service.State from, final Throwable failure) {
            serviceLatch.countDown();
            LOG.info("Service {} failed, latch counted down: {}", serviceName, serviceLatch.getCount());
        }
    }
}
