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

import com.korwe.thecore.dto.syndication.SyndicationEntry;
import com.korwe.thecore.dto.syndication.SyndicationFeed;
import com.korwe.thecore.service.SyndicationService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class SyndicationServiceImplTest {

    private SyndicationService service;

    @Before
    public void setup() {
        service = new SyndicationServiceImpl();
    }

    @Test
    public void fetchShouldWorkWithAtom() throws Exception {
        String feedUrl = "http://www.theregister.co.uk/headlines.atom";
        SyndicationFeed feed = service.fetchLatest(feedUrl, 5);
        assertNotNull(feed);
        assertNotNull(feed.getEntries());
        assertEquals(5, feed.getEntries().size());
        for (SyndicationEntry entry: feed.getEntries()) {
            assertNotNull(entry);
            System.out.println("Atom entry = " + entry);
        }
    }

    @Test
    public void fetchShouldWorkWithRss() throws Exception {
        String feedUrl = "http://feeds.bbci.co.uk/news/rss.xml";
        SyndicationFeed feed = service.fetchLatest(feedUrl, 5);
        assertNotNull(feed.getEntries());
        assertEquals(5, feed.getEntries().size());
        for (SyndicationEntry entry: feed.getEntries()) {
            assertNotNull(entry);
            System.out.println("RSS entry = " + entry);
        }
    }
}
