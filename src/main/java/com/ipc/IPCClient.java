package com.ipc;

import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.SolclientException;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.ContextHandle;
import com.solacesystems.solclientj.core.handle.Handle;
import com.solacesystems.solclientj.core.handle.SessionHandle;
import com.solacesystems.solclientj.core.handle.MessageHandle;
import com.solacesystems.solclientj.core.impl.ContextHandleImpl;
import com.solacesystems.solclientj.core.impl.SessionHandleImpl;
import com.solacesystems.solclientj.core.impl.TopicImpl;
import com.solacesystems.solclientj.core.resource.Topic;

public class IPCClient {

    public static void main(String[] args) throws Exception {

        // 1️⃣ Initialize Solace native API
        Solclient.init(new String[0]);

        // 2️⃣ Create CONTEXT HANDLE (implementation)
        ContextHandle context = new ContextHandleImpl();

        int ctxRc = Solclient.createContextForHandle(
                context,
                new String[0]
        );

        if (ctxRc != 0) {
            throw new SolclientException(
                    "createContextForHandle failed rc=" , ctxRc);
        }

        // 3️⃣ Create SESSION HANDLE (implementation)
        SessionHandle session = new SessionHandleImpl();

        // Connect to server (TCP IPC)
        String[] sessionProps = new String[] {
                SessionHandle.PROPERTIES.HOST, "tcp:127.0.0.1:55555",
                SessionHandle.PROPERTIES.USERNAME, "client"
        };

        int sessRc = context.createSessionForHandle(
                session,
                sessionProps,

                // Message callback (for replies if server sends any)
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
                                "[CLIENT] Event: " + s);
                    }
                }
        );

        if (sessRc != 0) {
            throw new SolclientException(
                    "createSessionForHandle failed rc=" , sessRc);
        }

        // 4️⃣ Connect session
        session.connect();

        System.out.println(
                "[CLIENT] Connected to tcp:127.0.0.1:55555");

        // 5️⃣ Create and send message
        MessageHandle msg = Solclient.createNewMessageAndHandle();
        msg.setBinaryAttachment(
                ("Hello from IPC client @ " + System.currentTimeMillis())
                        .getBytes()
        );

        Topic topic = new TopicImpl("ipc/demo");
        msg.setDestination(topic);
        session.send(msg);

        System.out.println("[CLIENT] Message sent");

        // Give some time to receive responses (if any)
        Thread.sleep(2000);
    }
}
