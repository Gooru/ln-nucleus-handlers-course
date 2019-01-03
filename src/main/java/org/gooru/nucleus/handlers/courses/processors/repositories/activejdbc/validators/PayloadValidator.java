package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

import java.util.Set;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 17/10/16.
 */
public interface PayloadValidator {

    default JsonObject validatePayload(JsonObject input, FieldSelector selector, ValidatorRegistry registry) {
        JsonObject result = new JsonObject();
        input.forEach(entry -> {
            if (selector.allowedFields().contains(entry.getKey())) {
                FieldValidator validator = registry.lookupValidator(entry.getKey());
                if (validator != null) {
                    if (!validator.validateField(entry.getValue())) {
                        result.put(entry.getKey(), "Invalid value");
                    }
                }
            } else {
                result.put(entry.getKey(), "Field not allowed");
            }
        });
        Set<String> mandatory = selector.mandatoryFields();
        if (mandatory != null && !mandatory.isEmpty()) {
            mandatory.forEach(s -> {
                if (input.getValue(s) == null) {
                    result.put(s, "Missing field");
                }
            });
        }
        return result.isEmpty() ? null : result;
    }
}
