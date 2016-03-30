package com.korwe.thecore.messages;

import com.korwe.thecore.client.AbstractServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public abstract class AbstractMessageContext<S extends AbstractServiceClient>{

    private Logger log = LoggerFactory.getLogger(AbstractMessageContext.class);

    protected S delegate;
    protected long timeout = 3000L;

    public AbstractMessageContext(S cAbstractServiceClient) {
        this.delegate = cAbstractServiceClient;
    }


    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean ping(){
        return false;
    }

    public void reset(){
        this.timeout = 3000L;
    }

    public AbstractMessageContext withTimeout(Long timeout){
        log.debug("Setting timeout to: {} ms", timeout);
        this.timeout = timeout;
        return this;
    }
}
