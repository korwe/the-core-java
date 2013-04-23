package com.korwe.thecore.router.basic;

import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.router.AmqpUriPart;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
@Component
public class CoreToServiceRoute extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(String.format("%s%s//%s?%s", AmqpUriPart.DirectPrefix.getValue(), MessageQueue.DIRECT_EXCHANGE,
                           MessageQueue.ClientToCore.getQueueName(), AmqpUriPart.Options.getValue()))
                .recipientList(simple(String.format("%s%s/%s.${in.header.choreography}//?%s",
                                                    AmqpUriPart.TopicPrefix.getValue(), MessageQueue.TOPIC_EXCHANGE,
                                                    MessageQueue.CoreToService.getQueueName(),
                                                    AmqpUriPart.Options.getValue())));
    }
}
