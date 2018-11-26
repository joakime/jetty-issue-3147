package com.example.services;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/broadcast")
public class BroadcastServerEndpoint {
    private static final Logger log = Logger.getLogger(BroadcastServerEndpoint.class.getName());

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void onOpen(final Session session) {
        System.out.println("onOpen");
        sessions.add(session);
    }

    @OnClose
    public void onClose(final Session session) {
        System.out.println("onClose");
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        System.out.println("error at websocket channel, reason: " + ex.getMessage());
    }

    @OnMessage
    public void onMessage(final String message, final Session client) throws IOException, EncodeException {
        log.info(String.format("Server received message '%s'", message));
        for (final Session session : sessions) {
            log.info(String.format("Server sends message '%s' to session %s", message, session.getId()));
            session.getBasicRemote().sendText(message);
        }
    }
}