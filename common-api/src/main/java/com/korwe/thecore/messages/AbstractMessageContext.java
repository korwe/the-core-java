package com.korwe.thecore.messages;

import com.korwe.thecore.client.AbstractServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public abstract class AbstractMessageContext<SC extends AbstractServiceClient, S>{

    private Logger log = LoggerFactory.getLogger(AbstractMessageContext.class);

    protected SC delegate;
    protected long timeout = 3000L;

    public AbstractMessageContext(SC cAbstractServiceClient) {
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

    public S reset(){
        this.timeout = 3000L;
        return (S)this;
    }

    public S withTimeout(Long timeout){
        log.debug("Setting timeout to: {} ms", timeout);
        this.timeout = timeout;
        return (S)this;
    }
}
