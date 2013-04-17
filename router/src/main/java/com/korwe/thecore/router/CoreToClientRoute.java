package com.korwe.thecore.router;

import com.korwe.thecore.api.MessageQueue;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
@Component
public class CoreToClientRoute extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(String.format("%s%s//%s?%s", AmqpUriPart.DirectPrefix.getValue(), MessageQueue.DIRECT_EXCHANGE,
                           MessageQueue.ServiceToCore.getQueueName(), AmqpUriPart.Options.getValue()))
                .recipientList(simple(String.format("%s%s/%s.${in.header.sessionId}//?%s",
                                                    AmqpUriPart.TopicPrefix.getValue(), MessageQueue.TOPIC_EXCHANGE,
                                                    MessageQueue.CoreToClient.getQueueName(),
                                                    AmqpUriPart.Options.getValue())));
    }
}
