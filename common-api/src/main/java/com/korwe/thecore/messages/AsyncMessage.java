package com.korwe.thecore.messages;

import java.util.function.Function;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public interface AsyncMessage<S> {
    public S async();
    public S async(Function callback);
}
