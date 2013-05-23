package com.korwe.thecore.router;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public enum AmqpUriPart {
    DirectPrefix("amqp:queue:BURL:direct://"),
    TopicPrefix("amqp:topic:BURL:topic://"),
    Options("exchangePattern=InOnly&disableReplyTo=true");

    private final String value;

    AmqpUriPart(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
