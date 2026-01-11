package com.ipc;

import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.ContextHandle;
import com.solacesystems.solclientj.core.handle.Handle;
import com.solacesystems.solclientj.core.handle.MessageHandle;
import com.solacesystems.solclientj.core.handle.SessionHandle;
import com.solacesystems.solclientj.core.impl.ContextHandleImpl;
import com.solacesystems.solclientj.core.impl.SessionHandleImpl;
import com.solacesystems.solclientj.core.impl.TopicImpl;
import com.solacesystems.solclientj.core.resource.Topic;

import java.nio.ByteBuffer;

public class PongService {

    private static final String PING_TOPIC = "ipc/ping";
    private static final String PONG_TOPIC = "ipc/pong";

    public static void main(String[] args) throws Exception {

        System.setProperty("scijava.nativelib.disable", "true");
        Solclient.init(new String[0]);

        ContextHandle context = new ContextHandleImpl();
        Solclient.createContextForHandle(context, new String[0]);

        SessionHandle session = new SessionHandleImpl();

        String[] props = new String[]{
                SessionHandle.PROPERTIES.HOST, "listen:127.0.0.1:55555",
                SessionHandle.PROPERTIES.USERNAME, "pong-service"
        };

        context.createSessionForHandle(
                session,
                props,

                // Receive PING
                new MessageCallback() {
                    @Override
                    public void onMessage(Handle h) {
                        try {
                            SessionHandleImpl msg = (SessionHandleImpl) h;

                            // ---- READ PAYLOAD CORRECTLY ----
                            MessageHandle messageHandle = msg.getRxMessage();
                            int size = messageHandle.getBinaryAttachmentSize();
                            if (size < Long.BYTES) return;

                            ByteBuffer buffer = ByteBuffer.allocate(size);
                            buffer.clear();
                            messageHandle.getBinaryAttachment(buffer);

                            long pingSendTime = buffer.getLong(0);
                            long pongReceiveTime = System.nanoTime();

                            long oneWayMicros =
                                    (pongReceiveTime - pingSendTime) / 1_000;

                            System.out.println(
                                    "[PONG] One-way latency = " + oneWayMicros + " µs"
                            );

                            // optional: echo payload back
                            MessageHandle reply = Solclient.createNewMessageAndHandle();
                            reply.setBinaryAttachment(buffer.array());
                            reply.setDestination(new TopicImpl("ipc/pong"));
                            session.send(reply);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },

                new SessionEventCallback() {
                    @Override
                    public void onEvent(SessionHandle s) {
                        System.out.println("[PONG] Event: " + s);
                    }
                }
        );

        session.connect();

        // Subscribe to PING
        Topic pingTopic = new TopicImpl(PING_TOPIC);
        session.subscribe(pingTopic, 0, 0);

        System.out.println("[PONG] Listening");
        Thread.currentThread().join();
    }
}
