package com.gym.crm.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

public class JsonReaderUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static <T> T readFromJson(String filePath, TypeReference<T> typeReference) {
        try (InputStream inputStream = JsonReaderUtil.class.getClassLoader().getResourceAsStream(filePath)) {
            validatePath(inputStream, filePath);

            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }

    public static <T> T readFromJson(String filePath, Class<T> clazz) {
        try (InputStream inputStream = JsonReaderUtil.class.getClassLoader().getResourceAsStream(filePath)) {
            validatePath(inputStream, filePath);

            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }

    private static void validatePath(InputStream inputStream, String filePath) {
        if (inputStream == null) {
            throw new RuntimeException("JSON file not found: " + filePath);
        }
    }
}
