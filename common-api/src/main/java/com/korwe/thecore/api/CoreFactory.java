package com.korwe.thecore.api;

/**
 *
 */
public interface CoreFactory {

    CoreSender createSender(MessageQueue messageQueue, String clientId);
    CoreSubscriber createSubscriber(MessageQueue messageQueue, String clientId);
}
