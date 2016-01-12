package com.korwe.thecore.api;

import com.korwe.thecore.messages.CoreMessageXmlSerializer;
import org.apache.qpid.transport.Connection;

/**
 * @author <a href="mailto:dario.matonicki@korwe.com>Dario Matonicki</a>
 */
public class CoreConnectionSharingSender extends CoreSender {

    protected CoreConnectionSharingSender(MessageQueue queue, Connection sharedConnection) {
        this.queue = queue;
        this.connection = sharedConnection;

        session = connection.createSession();
        serializer = new CoreMessageXmlSerializer();
    }

    @Override
    public void close() {
        session.sync();
        session.close();
    }
}
