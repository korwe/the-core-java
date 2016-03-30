package com.korwe.thecore.messages;

import java.util.function.Function;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public interface AsyncMessage<AMC extends AbstractMessageContext> {
    public AMC async();
    public AMC async(Function callback);
}
