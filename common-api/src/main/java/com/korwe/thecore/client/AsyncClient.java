package com.korwe.thecore.client;

import java.util.function.Function;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public interface AsyncClient<C> {
    public C async();
    public C async(Function callback);
}
