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

package com.korwe.thecore.webcore.servlet;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.jolbox.bonecp.BoneCPDataSource;
import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.CoreMessageSerializer;
import com.korwe.thecore.messages.CoreMessageXmlSerializer;
import com.korwe.thecore.webcore.DataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.*;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
@Singleton
public class WebCoreServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(WebCoreServlet.class);

    private static final String FETCH_MESSAGE_SQL =
            "SELECT message " +
            "  FROM received_message " +
            "  WHERE queue_name = ? and sessionId = ? and guid = ?";

    private BoneCPDataSource ds;
    private CoreSender messageSender;
    private CoreMessageSerializer serializer;

    public WebCoreServlet() {
        serializer = new CoreMessageXmlSerializer();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        ds = DataSourceProvider.createDataSource();
        messageSender = new CoreSender(MessageQueue.ClientToCore);
        LOG.info("Servlet init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String decodedPath = URLDecoder.decode(req.getPathInfo(), "UTF-8");
        Iterator<String> splitPath = Splitter.on('/').omitEmptyStrings().trimResults().split(decodedPath).iterator();
        LOG.debug("req = " + decodedPath);
        String queue = splitPath.hasNext() ? splitPath.next() : null;
        LOG.debug("queue = " + queue);
        String sessionId = splitPath.hasNext() ? splitPath.next() : null;
        LOG.debug("sessionId = " + sessionId);
        String guid = splitPath.hasNext() ? splitPath.next() : null;
        LOG.debug("guid = " + guid);


        try {
            List<String> messages = fetchMessages(queue, sessionId, guid);
            if (messages.isEmpty()) {
                LOG.debug("error: no matching message received");
                resp.getWriter().println("error: no matching message received");
            }
            else {
                LOG.debug(Joiner.on("\n").join(messages));
                resp.setContentType("text/xml");
                resp.getWriter().println(Joiner.on("\r\n").join(messages));
            }
        }
        catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private List<String> fetchMessages(String queue, String sessionId, String guid) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            connection = ds.getConnection();
            statement = connection.prepareStatement(FETCH_MESSAGE_SQL);
            statement.setString(1, queue);
            statement.setString(2, sessionId);
            statement.setString(3, guid);
            results = statement.executeQuery();
            List<String> messages = Lists.newLinkedList();
            while (results.next()) {
                messages.add(results.getString("message"));
            }
            return messages;
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = req.getParameter("message");
        if (Strings.isNullOrEmpty(message)) {
            LOG.debug("no message supplied");
            resp.getWriter().println("no message supplied");
        }
        else {
            this.serializer = new CoreMessageXmlSerializer();
            CoreMessage coreMessage = serializer.deserialize(message);
            String webcoreSessionId = "webcore." + coreMessage.getSessionId();
            coreMessage.setSessionId(webcoreSessionId);
            LOG.debug("Sending to core: coreMessage");
            messageSender.sendMessage(coreMessage);
            resp.getWriter().println("success sessionId = " + webcoreSessionId);
        }
    }

    @Override
    public void destroy() {
        LOG.info("Servlet destroy");
        ds.close();
        messageSender.close();
        super.destroy();
    }
}
