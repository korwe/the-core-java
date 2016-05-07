package com.korwe.thecore.testclient;

import com.korwe.thecore.api.CoreFactoryImpl;
import com.korwe.thecore.client.*;
import com.korwe.thecore.messages.CoreMessageXmlSerializer;
import com.korwe.thecore.messages.ServiceRequest;
import com.rabbitmq.client.ConnectionFactory;
import com.thoughtworks.xstream.XStream;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class TestAsyncClient {

    private static final String CLIENT_ID = "test-client-001";
    public static final String BBC = "http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml";
    public static final String GOOGLE = "http://news.google.com/?output=rss";
    public static final String NEWS24 = "http://feeds.news24.com/articles/news24/SouthAfrica/rss";

    private final XStream xStream;

    private CoreClient client;


    public TestAsyncClient() {
        xStream = new XStream();
        client = new CoreClient(CLIENT_ID, new XStreamSerializationStrategy(xStream), new CoreFactoryImpl(new ConnectionFactory(), new CoreMessageXmlSerializer()));
    }

    private ClientServiceRequest createRequest(String url, int maxEntries) {
        ClientServiceRequest clientServiceRequest = new ClientServiceRequest("SyndicationService", "fetchLatest");
        clientServiceRequest.setParameter("feedUrl", url);
        clientServiceRequest.setParameter("maxEntries", maxEntries);
        return clientServiceRequest;
    }

    public static void main(String[] args) throws InterruptedException {
        TestAsyncClient testAsyncClient = new TestAsyncClient();
        ClientServiceRequest req1 = testAsyncClient.createRequest(BBC, 70);
        ClientServiceRequest req4 = testAsyncClient.createRequest(BBC, 4);
        ClientServiceRequest req5 = testAsyncClient.createRequest(BBC, 5);
        ClientServiceRequest req2 = testAsyncClient.createRequest(GOOGLE, 50);
        ClientServiceRequest req3 = testAsyncClient.createRequest(NEWS24, 3);
        testAsyncClient.client.initSession();

//        testAsyncClient.client.makeRequests(10000L, req1);
//        testAsyncClient.client.makeRequests(7000L, req5, req4);
        Map<String, ServiceResult> results = testAsyncClient.client.makeRequests (3000L, req1, req2, req3);
        System.out.println("results = " + results);
        testAsyncClient.client.closeSession();
        Thread.sleep(1000L);
    }
}
