package com.mailmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
@EnableAsync
public class MailMindApplication {
    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(MailMindApplication.class, args);
    }

    private static void loadDotEnv() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            envFile = new File("../.env");
        }
        System.out.println("[MailMind] Looking for .env file at: " + envFile.getAbsolutePath());
        if (envFile.exists()) {
            System.out.println("[MailMind] Found .env file, loading variables...");
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int equalsIdx = line.indexOf('=');
                    if (equalsIdx > 0) {
                        String key = line.substring(0, equalsIdx).trim();
                        String value = line.substring(equalsIdx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        System.out.println("[MailMind] Setting property: " + key + " = " + (key.contains("SECRET") || key.contains("KEY") || key.contains("PASSWORD") ? "******" : value));
                        System.setProperty(key, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("[MailMind] Failed to load .env file: " + e.getMessage());
            }
        } else {
            System.err.println("[MailMind] .env file not found!");
        }
    }
}
