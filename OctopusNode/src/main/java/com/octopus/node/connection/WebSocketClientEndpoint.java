package com.octopus.node.connection;

import com.octopus.transport.decoders.ServerMessageDecoder;
import com.octopus.transport.decoders.SubJobDecoder;
import com.octopus.transport.encoders.Encoders;
import org.apache.log4j.Logger;

import javax.websocket.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@ClientEndpoint(
        decoders = { ServerMessageDecoder.class, SubJobDecoder.class },
        encoders = { Encoders.NodeRegistrationRequestEncoder.class, Encoders.SubJobResultEncoder.class, Encoders.SubJobUpdateEncoder.class })
public class WebSocketClientEndpoint {
    private final static Logger LOG = Logger.getLogger(WebSocketClientEndpoint.class);
    private final NodeClient nodeClient;

    private final Map<Class, MessageHandlerHelper.CustomMessageHandler> messageHandlers = new HashMap<>();

    public Session session = null;

    public WebSocketClientEndpoint(NodeClient nodeClient, URI endpointURI) throws Exception {
        this.nodeClient = nodeClient;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpointURI);
    }

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("Web socket opened with session id: " + session.getId());
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.info("closing web socket" + reason);
        this.session = null;
        // Let the node client handle cases where the socket is closed abnormally
        if (!CloseReason.CloseCodes.NORMAL_CLOSURE.equals(reason.getCloseCode())) {
            nodeClient.handleAbnormalSocketClose(reason);
        }
    }

    /**
     * For each message, an attempt is made to get the correct handler to handle the message.
     *
     * @param message {@link Object}
     */
    @OnMessage
    public void onMessage(Object message) {
        MessageHandlerHelper.CustomMessageHandler messageHandler = messageHandlers.getOrDefault(message.getClass(), MessageHandlerHelper.DEFAULT);
        messageHandler.handle(message);
    }

    public void sendMessage(Object message) {
        session.getAsyncRemote().sendObject(message);
    }

    public void addMessageHandler(Class messageType, MessageHandlerHelper.CustomMessageHandler messageHandler) {
        if (!messageHandlers.containsKey(messageType)) {
            messageHandlers.put(messageType, messageHandler);
        }
    }
}