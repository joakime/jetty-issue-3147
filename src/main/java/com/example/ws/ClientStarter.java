package com.example.ws;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.util.component.LifeCycle;
import com.example.services.BroadcastClientEndpoint;

public class ClientStarter
{
    public static void main(final String[] args) throws Exception
    {
        String clientId = UUID.randomUUID().toString().substring(0, 8);

        if (args.length > 0)
        {
            clientId = args[0];
        }

        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        final String uri = "ws://localhost:8080/broadcast";

        try (Session session = container.connectToServer(BroadcastClientEndpoint.class, URI.create(uri)))
        {
            for (int i = 1; i <= 1000; ++i)
            {
                TimeUnit.SECONDS.sleep(5);
                session.getBasicRemote().sendText(String.format("Message %,d from JavaClient [%s]", i, clientId));
            }
        }

        // JSR-356 has no concept of Container lifecycle.
        // (This is an oversight on the spec's part)
        // This stops the lifecycle of the Client WebSocketContainer
        if (container instanceof LifeCycle)
        {
            ((LifeCycle) container).stop();
        }
    }
}

