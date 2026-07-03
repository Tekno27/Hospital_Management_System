package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppSettings {

    private static final Path SETTINGS_FILE = Path.of(System.getProperty("user.dir"), "medcore_settings.properties");
    private static final Properties props = new Properties();
    private static boolean loaded;

    private AppSettings() {
    }

    public static void load() {
        if (loaded) {
            return;
        }
        if (Files.exists(SETTINGS_FILE)) {
            try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not load settings: " + e.getMessage());
            }
        }
        setDefault("hospitalName", "MedCore HMS");
        setDefault("emailNotifications", "true");
        setDefault("compactTables", "false");
        setDefault("showCharts", "true");
        setDefault("autoRefreshDashboard", "true");
        loaded = true;
    }

    private static void setDefault(String key, String value) {
        if (!props.containsKey(key)) {
            props.setProperty(key, value);
        }
    }

    public static void save() {
        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "MedCore HMS Settings");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        load();
        return props.getProperty(key);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static void set(String key, String value) {
        load();
        props.setProperty(key, value);
    }

    public static void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }
}
