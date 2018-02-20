package net.senmori.btsuite.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {

    private static File file = new File("src/main/resources/config.properties");

    public static final Properties properties = new Properties();

    public static void init() throws IOException {
        properties.load(new FileInputStream(file));
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String get(String key) {
        return getProperties().getProperty(key);
    }

    public static void set(String key, String value) {
        getProperties().setProperty(key, value);
    }
}
