package com.ipc;

import com.solacesystems.solclientj.core.*;
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

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingService {

    private static final String PING_TOPIC = "ipc/ping";
    private static final String PONG_TOPIC = "ipc/pong";

    public static void main(String[] args) throws Exception {

        System.setProperty("scijava.nativelib.disable", "true");
        Solclient.init(new String[0]);

        ContextHandle context = new ContextHandleImpl();
        Solclient.createContextForHandle(context, new String[0]);

        SessionHandle session = new SessionHandleImpl();

        String[] props = new String[]{
                //tcp or shm
                SessionHandle.PROPERTIES.HOST, "shm:127.0.0.1:55555",
                SessionHandle.PROPERTIES.USERNAME, "ping-service"
        };

        context.createSessionForHandle(
                session,
                props,

                // Receive PONG replies
                new MessageCallback() {
                    @Override
                    public void onMessage(Handle h) {
                        try {
                            SessionHandleImpl sh = (SessionHandleImpl) h;
                            MessageHandle msg =  sh.getRxMessage();
                            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                            buffer.putLong(System.nanoTime());

                            msg.setBinaryAttachment(buffer.array());
                            msg.setDestination(new TopicImpl("ipc/ping"));

                            session.send(msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                ,

                new SessionEventCallback() {
                    @Override
                    public void onEvent(SessionHandle s) {
                        System.out.println("[PING] Event: "+ s);
                    }
                }
        );

        session.connect();

        // Subscribe to PONG replies
        Topic pongTopic = new TopicImpl(PONG_TOPIC);
        session.subscribe(pongTopic, 0, 0);

        System.out.println("[PING] Started");

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                MessageHandle msg = Solclient.createNewMessageAndHandle();
                msg.setBinaryAttachment(longToBytes(System.nanoTime()));
                msg.setDestination(new TopicImpl(PING_TOPIC));

                session.send(msg);
                System.out.println("[PING] Sent ping");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private static byte[] longToBytes(long v) {
        byte[] b = new byte[8];
        for (int i = 7; i >= 0; i--) {
            b[i] = (byte) (v & 0xff);
            v >>= 8;
        }
        return b;
    }

    private static long bytesToLong(byte[] b) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v = (v << 8) | (b[i] & 0xff);
        }
        return v;
    }
}
