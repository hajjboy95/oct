package com.octopus.transport.decoders;

import com.google.gson.Gson;
import com.octopus.transport.ServerMessage;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class ServerMessageDecoder implements Decoder.Text<ServerMessage> {
    private static Gson parser = new Gson();

    @Override
    public boolean willDecode(String s) {
        try {
            ServerMessage serverMessage = parser.fromJson(s, ServerMessage.class);

            // If message is null, this object is not a ServerMessage
            return serverMessage.getMessage() != null;
        } catch (Exception e) {
            // If failed parsing from Json, this object is not a ServerMessage
            return false;
        }
    }

    @Override
    public ServerMessage decode(String s) {
        return parser.fromJson(s, ServerMessage.class);
    }

    @Override
    public void init(EndpointConfig config) {}

    @Override
    public void destroy() {}
}
