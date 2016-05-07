package com.korwe.thecore.router.basic;

import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.router.AmqpUriPart;
import org.apache.camel.component.rabbitmq.RabbitMQConstants;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
@Component
public class ServiceToCoreRoute extends SpringRouteBuilder {

    @Value("${amqp.host}")
    private String hostname;

    @Value("${amqp.port}")
    private String port;

    @Override
    public void configure() throws Exception {
        from(String.format("rabbitmq://%s:%s/%s?exchangeType=direct&declare=false&queue=%s&%s", hostname, port, MessageQueue.DIRECT_EXCHANGE,
                           MessageQueue.ServiceToCore.getQueueName(), AmqpUriPart.Options.getValue()))
                .setHeader(RabbitMQConstants.ROUTING_KEY).simple(String.format("%s.${in.header.sessionId}", MessageQueue.CoreToClient.getQueueName()))
                .removeHeader(RabbitMQConstants.EXCHANGE_NAME)
                .recipientList(simple(String.format("rabbitmq://%s:%s/%s?exchangeType=topic&declare=false&%s,rabbitmq://%s:%s/%s?exchangeType=direct&declare=false&queue=%s&%s",
                                                    hostname, port, MessageQueue.TOPIC_EXCHANGE,
                                                    AmqpUriPart.Options.getValue(),
                                                    hostname, port,
                                                    MessageQueue.DIRECT_EXCHANGE,
                                                    MessageQueue.Trace.getQueueName(),
                                                    AmqpUriPart.Options.getValue())));
    }

}
