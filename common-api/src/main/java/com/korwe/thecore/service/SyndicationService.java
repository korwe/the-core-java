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

import com.korwe.thecore.annotation.ParamNames;
import com.korwe.thecore.dto.syndication.SyndicationEntry;
import com.korwe.thecore.dto.syndication.SyndicationFeed;

import java.util.List;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public interface SyndicationService extends PingService {
    public static final String serviceName = "SyndicationService";
    @ParamNames({"feedUrl", "maxEntries"})
    public SyndicationFeed fetchLatest(String feedUrl, int maxEntries);
}
