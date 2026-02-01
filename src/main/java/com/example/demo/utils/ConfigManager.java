package com.example.demo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 简化版配置管理：
 * - 目前主要用于保存 WebServer 端口等少量配置
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "toolkit.properties";
    private static ConfigManager instance;

    private final Properties properties = new Properties();

    private ConfigManager() {
        load();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void load() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
        } catch (IOException ignored) {
        }
    }

    private void save() {
        File file = new File(CONFIG_FILE);
        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, "Toolkit Config");
        } catch (IOException ignored) {
        }
    }

    public int getWebServerPort() {
        String val = properties.getProperty("web.port", "8081");
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 8081;
        }
    }

    public void setWebServerPort(int port) {
        properties.setProperty("web.port", String.valueOf(port));
        save();
    }
}

