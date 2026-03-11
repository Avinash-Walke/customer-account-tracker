package com.customer.account.tracker.annotation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

public class JsonFileArgumentsProvider implements ArgumentsProvider {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        JsonFileSource annotation = context.getRequiredTestMethod().getAnnotation(JsonFileSource.class);
        if (annotation == null) {
            return Stream.empty();
        }

        Class<?> targetType = annotation.returnType();
        String[] resources = annotation.jsonPath();

        if (resources == null || resources.length == 0) {
            throw new IllegalArgumentException("@JsonFileSource must specify at least one resource path");
        }

        // Require an explicit, non-String target type
        if (targetType == null || targetType.equals(Void.class) || targetType.equals(String.class)) {
            throw new IllegalArgumentException("@JsonFileSource requires a file path of json and a target deserialization type");
        }

        if (resources.length == 1) {
            Object obj = readResourceAsObject(resources[0], targetType);
            return Stream.of(Arguments.of(obj));
        }

        // multiple resources -> deserialize each into the same target type and supply them as separate parameters
        Object[] deserialized = Arrays.stream(resources)
                .map(resource -> readResourceAsObject(resource, targetType))
                .toArray(Object[]::new);

        return Stream.of(Arguments.of(deserialized));
    }

    private Object readResourceAsObject(String resource, Class<?> targetType) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found on classpath: " + resource);
            }
            return objectMapper.readValue(is, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read or deserialize JSON fixture '" + resource + "' to " + targetType.getSimpleName(), e);
        }
    }
}