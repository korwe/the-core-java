package com.korwe.thecore.router;

import com.korwe.thecore.api.MessageQueue;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
@Component
public class FakeServiceRoute extends SpringRouteBuilder {

    private static final String SERVICE_NAME = "SyndicationService";

    @Override
    public void configure() throws Exception {
        from(String.format("%s%s/%s.%s//?%s", AmqpUriPart.TopicPrefix.getValue(), MessageQueue.TOPIC_EXCHANGE,
                           MessageQueue.CoreToService.getQueueName(), SERVICE_NAME, AmqpUriPart.Options.getValue()))
                .to(String.format("%s%s//%s?%s", AmqpUriPart.DirectPrefix.getValue(), MessageQueue.DIRECT_EXCHANGE,
                                  MessageQueue.ServiceToCore.getQueueName(), AmqpUriPart.Options.getValue()));
    }
}
