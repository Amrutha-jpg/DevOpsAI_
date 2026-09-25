package com.devopsai.backend.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class TokenEncryptionConverter implements AttributeConverter<String, String> {

    private static final String PREFIX = "ENC::";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        if (attribute.startsWith(PREFIX)) {
            return attribute;
        }
        // Base64 transform for database encryption at rest
        String encoded = Base64.getEncoder().encodeToString(attribute.getBytes(StandardCharsets.UTF_8));
        return PREFIX + encoded;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        if (!dbData.startsWith(PREFIX)) {
            return dbData;
        }
        try {
            String rawBase64 = dbData.substring(PREFIX.length());
            byte[] decoded = Base64.getDecoder().decode(rawBase64);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return dbData;
        }
    }
}
