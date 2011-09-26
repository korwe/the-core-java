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

import com.korwe.thecore.service.ping.PingServiceImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class SyndicationServiceImpl extends PingServiceImpl implements SyndicationService {

    private static final Logger LOG = Logger.getLogger(SyndicationService.class);

    @Override
    public List<SyndEntry> fetchLatest(String feedUrl, int maxEntries) {
        try {
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));
            List entries = feed.getEntries();
            if (null != entries) {
                List<SyndEntry> latest = new ArrayList<SyndEntry>(maxEntries);
                Iterator it = entries.iterator();
                for (int i = 0; it.hasNext() && i < maxEntries; i++) {
                    latest.add((SyndEntry) it.next());
                }
                return latest;
            }
        }
        catch (MalformedURLException e) {
            LOG.error("Invalid feed URL", e);
        }
        catch (FeedException e) {
            LOG.error("Feed could not be parsed", e);
        }
        catch (IOException e) {
            LOG.error("Feed could not be opened", e);
        }
        return null;
    }
}
