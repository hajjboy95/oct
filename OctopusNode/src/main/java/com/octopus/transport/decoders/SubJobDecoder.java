package com.octopus.transport.decoders;

import com.google.gson.Gson;
import com.octopus.transport.SubJob;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class SubJobDecoder implements Decoder.Text<SubJob> {
    private static Gson parser = new Gson();

    @Override
    public boolean willDecode(String s) {
        try {
            SubJob subJob = parser.fromJson(s, SubJob.class);

            // If parent id is null, this object is not a SubJob
            return subJob.getParentUUID() != null;
        } catch (Exception e) {
            // If failed parsing from Json, this object is not a SubJob
            return false;
        }
    }

    @Override
    public SubJob decode(String s) {
        return parser.fromJson(s, SubJob.class);
    }

    @Override
    public void init(EndpointConfig config) {}

    @Override
    public void destroy() {}
}
