package com.octopus.node.core;

import com.google.gson.Gson;

import java.util.List;

public class WebScrapingPayload {
    private final static Gson parser = new Gson();

    private final List<String> urls;
    private final List<String> scrapingTags;

    public static WebScrapingPayload from(Object data) {
        final String jsonStr = parser.toJson(data);
        return parser.fromJson(jsonStr, WebScrapingPayload.class);
    }

    public WebScrapingPayload(List<String> urls, List<String> scrapingTags) {
        this.urls = urls;
        this.scrapingTags = scrapingTags;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<String> getScrapingTags() {
        return scrapingTags;
    }
}
