package com.octopus.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OctopusNodeConfig {
    private final static String fileName = "src/main/resources/config.properties";

    public interface ConfigKeys {
        String WEBSOCKET_ENDPOINT = "websocket.endpoint";
        String NODE_ACCOUNT = "account.username";
    }

    public static Properties get() throws IOException {
        final Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(fileName);
        properties.load(inputStream);
        return properties;
    }
}
