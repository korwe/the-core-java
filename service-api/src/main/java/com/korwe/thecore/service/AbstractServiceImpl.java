package com.korwe.thecore.service;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public abstract class AbstractServiceImpl {

    private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public void async(Function toDo){
        executorService.submit( ()-> toDo.apply(null));
    }
}
