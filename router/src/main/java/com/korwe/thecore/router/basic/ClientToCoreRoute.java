package com.korwe.thecore.router.basic;

import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.*;
import com.korwe.thecore.router.AmqpUriPart;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
@Component
public class ClientToCoreRoute extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(String.format("%s%s//%s?%s", AmqpUriPart.DirectPrefix.getValue(), MessageQueue.DIRECT_EXCHANGE,
                           MessageQueue.ClientToCore.getQueueName(), AmqpUriPart.Options.getValue()))
            .choice()
                .when(header("messageType").isEqualTo(CoreMessage.MessageType.ServiceRequest.name()))
                    .recipientList(simple(String.format("%s%s/%s.${in.header.choreography}//?%s",
                                                        AmqpUriPart.TopicPrefix.getValue(), MessageQueue.TOPIC_EXCHANGE,
                                                        MessageQueue.CoreToService.getQueueName(),
                                                        AmqpUriPart.Options.getValue()))).end()
                .when(header("messageType").isEqualTo(CoreMessage.MessageType.InitiateSessionRequest.name()))
                    .process(new InitiateSessionProcessor())
                        .recipientList(simple(String.format("%s%s/%s.${in.header.sessionId}//?%s",
                                                            AmqpUriPart.TopicPrefix.getValue(),
                                                            MessageQueue.TOPIC_EXCHANGE,
                                                            MessageQueue.CoreToClient.getQueueName(),
                                                            AmqpUriPart.Options.getValue()))).end()
                .when(header("messageType").isEqualTo(CoreMessage.MessageType.KillSessionRequest.name()))
                    .process(new KillSessionProcessor())
                        .recipientList(simple(String.format("%s%s/%s.${in.header.sessionId}//?%s",
                                                            AmqpUriPart.TopicPrefix.getValue(),
                                                            MessageQueue.TOPIC_EXCHANGE,
                                                            MessageQueue.CoreToClient.getQueueName(),
                                                            AmqpUriPart.Options.getValue()))).end();

    }

    private class InitiateSessionProcessor implements Processor {

        CoreMessageSerializer serializer = new CoreMessageXmlSerializer();

        @Override
        public void process(Exchange exchange) throws Exception {
            Message in = exchange.getIn();
            String sessionId = in.getHeader("sessionId", String.class);
            String guid = in.getHeader("guid", String.class);
            InitiateSessionResponse response = new InitiateSessionResponse(sessionId, guid, true);
            in.setBody(serializer.serialize(response));
            in.setHeader("messageType", response.getMessageType().name());
        }
    }

    private class KillSessionProcessor implements Processor {

        CoreMessageSerializer serializer = new CoreMessageXmlSerializer();

        @Override
        public void process(Exchange exchange) throws Exception {
            Message in = exchange.getIn();
            String sessionId = in.getHeader("sessionId", String.class);
            String guid = in.getHeader("guid", String.class);
            KillSessionResponse response = new KillSessionResponse(sessionId, guid, true);
            in.setBody(serializer.serialize(response));
            in.setHeader("messageType", response.getMessageType().name());
        }
    }

}
