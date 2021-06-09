package ru.nya.push.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.nya.push.rest.BusinessRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author yapparova-nv on 28.04.2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T getObjectFromString(String string, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(string, clazz);
        } catch (IOException e) {
            throw new BusinessRuntimeException("Couldn't parse string");
        }
    }

    public static <T> T getObjectFromString(String string, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(string, typeReference);
        } catch (IOException e) {
            throw new BusinessRuntimeException("Couldn't parse string");
        }
    }

    public static <T> T getObjectFromReader(BufferedReader reader, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readerFor(clazz).readValue(reader);
        } catch (IOException e) {
            throw new BusinessRuntimeException("Couldn't parse string");
        }
    }

    public static <T> String getStringFromObject(T obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessRuntimeException("Couldn't parse object");
        }
    }

    public static ObjectWriter getObjectWriter(Class clazz) {
        return OBJECT_MAPPER.writerFor(clazz);
    }
}
