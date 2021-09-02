package com.octopus.node.core;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.nio.charset.StandardCharsets;

public class VideoPayload {
    private final static Gson parser = new Gson();

    private final byte[] videoBytes;
    private final String sourceEncoding;
    private final String targetEncoding;

    public static VideoPayload from(Object data) {
        final String jsonStr = parser.toJson(data);
        final LinkedTreeMap map = parser.fromJson(jsonStr, LinkedTreeMap.class);

        final String video = (String) map.get("video");
        final String sourceEncoding = (String) map.get("sourceEncoding");
        final String targetEncoding = (String) map.get("targetEncoding");

        return new VideoPayload(video.getBytes(StandardCharsets.UTF_8), sourceEncoding, targetEncoding);
    }

    public VideoPayload(byte[] videoBytes, String sourceEncoding, String targetEncoding) {
        this.videoBytes = videoBytes;
        this.sourceEncoding = sourceEncoding;
        this.targetEncoding = targetEncoding;
    }

    public byte[] getVideoBytes() {
        return videoBytes;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public String getTargetEncoding() {
        return targetEncoding;
    }
}
