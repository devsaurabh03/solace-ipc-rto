package com.ipc;

import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.SolclientException;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.ContextHandle;
import com.solacesystems.solclientj.core.handle.Handle;
import com.solacesystems.solclientj.core.handle.SessionHandle;
import com.solacesystems.solclientj.core.impl.ContextHandleImpl;
import com.solacesystems.solclientj.core.impl.SessionHandleImpl;
import com.solacesystems.solclientj.core.handle.MessageHandle;
import com.solacesystems.solclientj.core.impl.TopicImpl;
import com.solacesystems.solclientj.core.resource.Subscription;
import com.solacesystems.solclientj.core.resource.Topic;

public class IPCServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("scijava.nativelib.disable", "true");
        // 1️⃣ Initialize Solace native API
        Solclient.init(new String[0]);

        // 2️⃣ Create CONTEXT HANDLE (implementation class)
        ContextHandle context = new ContextHandleImpl();

        int ctxRc = Solclient.createContextForHandle(
                context,
                new String[0]
        );

        if (ctxRc != 0) {
            throw new SolclientException("createContextForHandle failed rc=" , ctxRc);
        }

        // 3️⃣ Create SESSION HANDLE (implementation class)
        SessionHandle session = new SessionHandleImpl();

        String[] sessionProps = new String[] {
                SessionHandle.PROPERTIES.HOST, "listen:127.0.0.1:55555",
                SessionHandle.PROPERTIES.USERNAME, "server"
        };

        int sessRc = context.createSessionForHandle(
                session,
                sessionProps,
                // Message callback
                new MessageCallback() {
                    @Override
                    public void onMessage(Handle hm) {
                        MessageHandle m =(MessageHandle) hm;
                        int payload = m.getBinaryAttachmentSize();
                        if (payload > 0) {
                            System.out.println(
                                    "[SERVER] Received: "
                            );
                        }
                    }
                },

                // Session event callback
                new SessionEventCallback() {
                    @Override
                    public void onEvent(SessionHandle s) {
                        System.out.println(
                                "[SERVER] Event: " + s);
                    }
                }
        );

        if (sessRc != 0) {
            throw new SolclientException(
                    "createSessionForHandle failed rc=" , sessRc);
        }

        // 4️⃣ Connect
        session.connect();
        // Create topic
        Topic topic = new TopicImpl("ipc/demo");

// Wrap topic in subscription

// Subscribe (flags = 0, timeout = 0 means block)
        int rc = session.subscribe(
                topic,
                0,      // flags
                0       // timeout (ms), 0 = block
        );

        if (rc != 0) {
            throw new SolclientException("Subscribe failed rc=" , rc);
        }

        System.out.println("[SERVER] Subscribed to ipc/demo");


        System.out.println(
                "[SERVER] IPC listening on listen:127.0.0.1:55555");

        Thread.currentThread().join();
    }
}
