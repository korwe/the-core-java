package com.korwe.thecore.testclient;


import com.korwe.thecore.api.*;

import com.korwe.thecore.messages.*;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class TestClient implements CoreMessageHandler {

    private static final String SESSION_ID = "test-session-001";
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private CoreSender sender;
    private CoreSubscriber subscriber;
    private CoreSubscriber dataSubscriber;
    private CoreMessageSerializer serializer = new CoreMessageXmlSerializer();
    private XStream xstream = new XStream();
    CoreFactory coreFactory = new CoreFactoryImpl(new ConnectionFactory(), serializer);

    private static final String MSG_FILE = "/msg.0.xml";

    private void connect() {
        sender = coreFactory.createSender(MessageQueue.ClientToCore, SESSION_ID);
        subscriber = coreFactory.createSubscriber(MessageQueue.CoreToClient, SESSION_ID);
        subscriber.connect(this);
        dataSubscriber = coreFactory.createSubscriber(MessageQueue.Data, SESSION_ID);
        dataSubscriber.connect(this);
    }

    private void close() {
        subscriber.close();
        dataSubscriber.close();
        sender.close();
    }

    private CoreMessage readMessage() throws IOException {
        String lineSep = System.getProperty("line.separator");
        BufferedReader msgFile = null;
        StringBuilder builder = new StringBuilder();
        try {
            msgFile = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(MSG_FILE),
                                                               Charset.forName("UTF-8")));
            String line = msgFile.readLine();

            while (line != null) {
                builder.append(line).append(lineSep);
                line = msgFile.readLine();
            }
        }
        finally {
            if (msgFile != null) {
                msgFile.close();
            }
        }
        return builder.length() > 0 ? serializer.deserialize(builder.toString()) : null;
    }

    private ServiceRequest createRequest() {
        ServiceRequest req = new ServiceRequest(SESSION_ID, "fetchLatest");
        req.setChoreography("SyndicationService");
        req.setParameter("feedUrl", xstream.toXML("http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml"));
        req.setParameter("maxEntries", xstream.toXML(10));
        return req;
    }

    private void sendMessage(CoreMessage message) {
        log.debug("Sending message: {}", message);
        sender.sendMessage(message);
    }

    @Override
    public void handleMessage(CoreMessage message) {
        log.debug("Received message: {}", message);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        TestClient client = new TestClient();
        client.connect();
        client.sendMessage(new InitiateSessionRequest(SESSION_ID));
        Thread.sleep(100L);
        client.sendMessage(client.createRequest());
        Thread.sleep(2000L);
        client.sendMessage(new KillSessionRequest(SESSION_ID));
        Thread.sleep(100L);
        client.close();
    }
}
