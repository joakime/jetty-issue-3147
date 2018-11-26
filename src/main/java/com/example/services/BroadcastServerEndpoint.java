package com.example.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/broadcast")
public class BroadcastServerEndpoint {
    private static final Logger log = Logger.getLogger(BroadcastServerEndpoint.class.getName());

    private Session session;

    @OnOpen
    public void onOpen(final Session session) {
        System.out.println("onOpen");
        this.session = session;
    }

    @OnClose
    public void onClose(final Session session) {
        System.out.println("onClose");
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        log.log(Level.WARNING, "Session " + session, ex);
        System.out.println("error at websocket channel, reason: " + ex.getMessage());
    }

    @OnMessage
    public void onMessage(final String message, final Session client) throws IOException, EncodeException {
        log.info(String.format("Server received message '%s'", message));
        for (final Session otherSession : session.getOpenSessions()) {
            if(otherSession == null) {
                log.info("Other Session was removed before I got a chance to use it");
                continue; // skip (this session was removed and invalidated between .getOpenSessions and actual iteration step)
            }

            if(!otherSession.isOpen()) {
                log.info(String.format("Other Session %s is no longer open", session));
                continue; // skip (this session is no longer open)
            }

            log.info(String.format("Server sends message '%s' to session %s", message, otherSession.getId()));
            RemoteEndpoint.Basic otherRemote = otherSession.getBasicRemote();
            if(otherRemote == null) {
                log.info(String.format("Other Session %s no longer has a valid remote", session));
                continue; // skip (this remote is no longer valid, like caught this in the process of close handshake)
            }

            try
            {
                // attempt to send the message
                otherRemote.sendText(message);
            } catch(IOException e) {
                log.log(Level.WARNING, String.format("Other Session %s remote is unable to send message", session), e);
                // skip, this endpoint's remote is unable to send this message.
                // likely because the endpoint was valid, was open, and between asking for the
                // the remote and actually sending the message the endpoint or connection or remote was closed.
            }
        }
    }
}