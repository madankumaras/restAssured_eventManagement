package com.eventmanagement.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try {
            String path = "src/test/resources/config.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            System.err.println("Configuration file not found. " + e.getMessage());
        }
    }

    public static String getProperty(String keyName) {
        return properties.getProperty(keyName);
    }
}
