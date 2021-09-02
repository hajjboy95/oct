package com.octopus.transport.encoders;

import com.google.gson.Gson;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

class TextEncoder<T> implements Encoder.Text<T> {
    private static Gson parser = new Gson();

    @Override
    public String encode(T object) throws EncodeException {
        return parser.toJson(object);
    }

    @Override
    public void init(EndpointConfig config) {}

    @Override
    public void destroy() {}
}