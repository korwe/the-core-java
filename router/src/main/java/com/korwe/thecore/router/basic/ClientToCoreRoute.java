package com.korwe.thecore.router.basic;

import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.*;
import com.korwe.thecore.router.AmqpUriPart;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.rabbitmq.RabbitMQConstants;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
@Component
public class ClientToCoreRoute extends SpringRouteBuilder {

    @Value("${amqp.host}")
    private String hostname;

    @Value("${amqp.port}")
    private String port;

    @Override
    public void configure() throws Exception {
        from(String.format("rabbitmq://%s:%s/%s?exchangeType=direct&declare=false&queue=%s&%s", hostname, port, MessageQueue.DIRECT_EXCHANGE,
                           MessageQueue.ClientToCore.getQueueName(), AmqpUriPart.Options.getValue()))
            .choice()
                .when(header("messageType").isEqualTo(CoreMessage.MessageType.ServiceRequest.name()))
                    .setHeader(RabbitMQConstants.ROUTING_KEY).simple(String.format("%s.${in.header.choreography}", MessageQueue.CoreToService.getQueueName()))
                    .removeHeader(RabbitMQConstants.EXCHANGE_NAME)
                    .recipientList(simple(String.format("rabbitmq://%s:%s/%s?exchangeType=topic&declare=false&%s,rabbitmq://%s:%s/%s?exchangeType=direct&declare=false&queue=%s&%s",
                                                        hostname, port, MessageQueue.TOPIC_EXCHANGE,
                                                        AmqpUriPart.Options.getValue(),
                                                        hostname, port,
                                                        MessageQueue.DIRECT_EXCHANGE,
                                                        MessageQueue.Trace.getQueueName(),
                                                        AmqpUriPart.Options.getValue()))).end()
                .when(header("messageType").isEqualTo(CoreMessage.MessageType.InitiateSessionRequest.name()))
                    .process(new SessionResponseProcessor() {
                        @Override
                        protected CoreResponse createResponse(String sessionId, String guid) {
                            return new InitiateSessionResponse(sessionId, guid, true);
                        }
                    })
                        .setHeader(RabbitMQConstants.ROUTING_KEY).simple(String.format("%s.${in.header.sessionId}", MessageQueue.CoreToClient.getQueueName()))
                        .removeHeader(RabbitMQConstants.EXCHANGE_NAME)
                        .recipientList(simple(String.format("rabbitmq://%s:%s/%s?exchangeType=topic&declare=false&%s",
                                                            hostname, port,
                                                            MessageQueue.TOPIC_EXCHANGE,
                                                            AmqpUriPart.Options.getValue()))).end()
                .when(header("messageType").isEqualTo(CoreMessage.MessageType.KillSessionRequest.name()))
                    .process(new SessionResponseProcessor() {
                        @Override
                        protected CoreResponse createResponse(String sessionId, String guid) {
                            return new KillSessionResponse(sessionId, guid, true);
                        }
                    })
                        .setHeader(RabbitMQConstants.ROUTING_KEY).simple(String.format("%s.${in.header.sessionId}", MessageQueue.CoreToClient.getQueueName()))
                        .removeHeader(RabbitMQConstants.EXCHANGE_NAME)
                        .recipientList(simple(String.format("rabbitmq://%s:%s/%s?exchangeType=topic&declare=false&%s",
                                                            hostname, port,
                                                            MessageQueue.TOPIC_EXCHANGE,
                                                            AmqpUriPart.Options.getValue()))).end();

    }

    private abstract class SessionResponseProcessor implements Processor {

        CoreMessageSerializer serializer = new CoreMessageXmlSerializer();

        @Override
        public void process(Exchange exchange) throws Exception {
            Message in = exchange.getIn();
            String sessionId = in.getHeader("sessionId", String.class);
            String guid = in.getHeader("guid", String.class);
            CoreResponse response = createResponse(sessionId, guid);
            in.setBody(serializer.serialize(response));
            in.setHeader("messageType", response.getMessageType().name());
        }

        protected abstract CoreResponse createResponse(String sessionId, String guid);
    }

}
