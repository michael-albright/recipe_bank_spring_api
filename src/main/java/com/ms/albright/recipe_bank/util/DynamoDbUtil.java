package com.ms.albright.recipe_bank.util;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamoDbUtil {

    public static Map<String, Object> convertItem(Map<String, AttributeValue> item) {
        Map<String, Object> result = new HashMap<>();
        item.forEach((key, value) -> {
            if (value.s() != null) {
                result.put(key, value.s()); // String value
            } else if (value.n() != null) {
                result.put(key, Double.parseDouble(value.n())); // Numeric value
            } else if (value.bool() != null) {
                result.put(key, value.bool()); // Boolean value
            } else if (value.ss() != null) {
                result.put(key, value.ss()); // String Set
            } else if (value.ns() != null) {
                result.put(key, value.ns()); // Number Set
            } else if (value.bs() != null) {
                result.put(key, value.bs()); // Binary Set
            } else if (value.m() != null) {
                result.put(key, convertItem(value.m())); // Map value, recursively convert
            } else if (value.l() != null) {
                result.put(key, value.l()); // List value (returns list of AttributeValues)
            } else {
                result.put(key, value.toString()); // Fallback, return as string
            }
        });
        return result;
    }
}