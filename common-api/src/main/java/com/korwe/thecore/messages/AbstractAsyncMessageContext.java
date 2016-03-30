package com.korwe.thecore.messages;

import com.google.common.util.concurrent.FutureCallback;
import com.korwe.thecore.client.AbstractServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class AbstractAsyncMessageContext<SC extends AbstractServiceClient> extends AbstractMessageContext<SC> implements AsyncMessage{

    private static long ASYNC_TIMEOUT = 300000L;
    private Logger log = LoggerFactory.getLogger(AbstractAsyncMessageContext.class);

    private boolean isAsync = false;
    private FutureCallback callback;

    public AbstractAsyncMessageContext(SC cAbstractServiceClient) {
        super(cAbstractServiceClient);
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public FutureCallback getCallback() {
        return callback;
    }

    public void setCallback(FutureCallback callback) {
        this.callback = callback;
    }

    @Override
    public AbstractMessageContext async(){
        this.isAsync = true;
        log.debug("In async mode");
        return withTimeout(ASYNC_TIMEOUT);
    }

    @Override
    public AbstractMessageContext async(java.util.function.Function callback) {
        this.isAsync = true;

        this.callback = new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                log.debug("Request succeeded, applying callback with result {}", result);
                callback.apply(result);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("Request failed, skipping callback");
                t.printStackTrace();
            }
        };

        return withTimeout(ASYNC_TIMEOUT);
    }

    @Override
    public void reset(){
        super.reset();
        this.callback = null;
        this.isAsync = false;
    }
}
