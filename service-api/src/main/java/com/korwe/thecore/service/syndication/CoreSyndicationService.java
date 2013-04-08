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

package com.korwe.thecore.service.syndication;

import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;
import com.korwe.thecore.service.SyndicationService;
import com.korwe.thecore.service.ping.CorePingService;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CoreSyndicationService extends CorePingService {

    private static final Logger LOG = Logger.getLogger(SyndicationService.class);
    private static final String FEED_ERROR = "The feed could not be updated";

    private SyndicationService syndicationService = new SyndicationServiceImpl();

    public CoreSyndicationService(int maxThreads) {
        super(maxThreads);
    }

    @Override
    protected void handleServiceRequest(ServiceRequest request) {
        String function = request.getFunction();
        if ("Ping".equalsIgnoreCase(function)) {
            handlePingRequest(request);
        }
        else if ("FetchLatest".equalsIgnoreCase(function)) {
            handleFetchLatestRequest(request);
        }
        else {
            handleUnsupportedFunctionRequest(request);
        }
    }

    private void handleFetchLatestRequest(ServiceRequest request) {
        String feedUrl = request.getParameterValue("feedUrl");
        String maxEntriesParam = request.getParameterValue("maxEntries");
        int maxEntries = 3;
        try {
            maxEntries = Integer.parseInt(maxEntriesParam);
        }
        catch(NumberFormatException nfe) {
            // Ignore, default to 1
        }
        List<SyndEntry> entries = syndicationService.fetchLatest(feedUrl, maxEntries);
        if (null == entries) {
            sendErrorResponse(request);
        }
        else {
            sendSuccessResponses(request, entries);
        }
    }

    private void sendSuccessResponses(ServiceRequest request, Iterable<SyndEntry> entries) {
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), true, true);
        sendResponse(response);
        StringBuilder entryBuilder = new StringBuilder("<entries>\n");
        for (SyndEntry entry: entries) {
            entryBuilder.append("  <entry>\n");
            entryBuilder.append("    <date>")
                    .append(null != entry.getUpdatedDate() ? entry.getUpdatedDate() : entry.getPublishedDate())
                    .append("</date>\n");
            entryBuilder.append("    <title>").append(entry.getTitle()).append("</title>\n");
            entryBuilder.append("    <description>")
                    .append(entry.getDescription().getValue())
                    .append("</description>\n");
            entryBuilder.append("    <link>").append(entry.getLink()).append("</link>\n");
            entryBuilder.append("  </entry>\n");
        }
        entryBuilder.append("</entries>");
        DataResponse dataResponse = new DataResponse(request.getSessionId(), request.getGuid(),
                                                     entryBuilder.toString());
        sendData(dataResponse);
    }

    private void sendErrorResponse(ServiceRequest request) {
        LOG.error(FEED_ERROR);
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorCode("FetchFailed");
        response.setErrorMessage(FEED_ERROR);
        sendResponse(response);
    }
}
