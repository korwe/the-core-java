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

package com.korwe.thecore.session;

import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.messages.CoreMessage;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public interface CoreMessageProcessor {

    public boolean shouldProcessMessage(final CoreMessage message);

    public void processMessage(final CoreMessage message);

    public void initialize(String sessionId);

    public String getSessionId();

    public void stop();

    public void setClientSender(CoreSender clientSender);
    public void setServiceSender(CoreSender serviceSender);
}
