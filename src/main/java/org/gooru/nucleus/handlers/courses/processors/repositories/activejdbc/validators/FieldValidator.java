package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

import java.util.Set;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhelpers.LanguageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 17/10/16.
 */
public interface FieldValidator {

  Logger LOGGER = LoggerFactory.getLogger(FieldValidator.class);

  static boolean validateStringIfPresent(Object o, int len) {
    return o == null
        || (o instanceof String && !((String) o).trim().isEmpty() && ((String) o).length() < len);
  }

  static boolean validateStringAllowedValue(Object o, Set<String> allowedValues) {
    return !(o == null || !allowedValues.contains(o));
  }

  static boolean validateStringIfPresentAllowEmpty(Object o, int len) {
    return o == null || (o instanceof String && ((String) o).length() < len);
  }

  static boolean validateString(Object o, int len) {
    return !(o == null || !(o instanceof String) || ((String) o).trim().isEmpty()
        || (((String) o).length() > len));
  }

  static boolean validateJsonIfPresent(Object o) {
    return o == null || o instanceof JsonObject && !((JsonObject) o).isEmpty();
  }

  static boolean validateJson(Object o) {
    return !(o == null || !(o instanceof JsonObject) || ((JsonObject) o).isEmpty());
  }

  static boolean validateJsonArrayIfPresent(Object o) {
    return o == null || o instanceof JsonArray && !((JsonArray) o).isEmpty();
  }

  static boolean validateJsonArray(Object o) {
    return !(o == null || !(o instanceof JsonArray) || ((JsonArray) o).isEmpty());
  }

  static boolean validateDeepJsonArrayIfPresent(Object o, FieldValidator fv) {
    if (o == null) {
      return true;
    } else if (!(o instanceof JsonArray) || ((JsonArray) o).isEmpty()) {
      return false;
    } else {
      JsonArray array = (JsonArray) o;
      for (Object element : array) {
        if (!fv.validateField(element)) {
          return false;
        }
      }
    }
    return true;
  }

  static boolean validateDeepJsonArray(Object o, FieldValidator fv) {
    if (o == null || !(o instanceof JsonArray) || ((JsonArray) o).isEmpty()) {
      return false;
    }
    JsonArray array = (JsonArray) o;
    for (Object element : array) {
      if (!fv.validateField(element)) {
        return false;
      }
    }
    return true;
  }

  static boolean validateBoolean(Object o) {
    return o != null && o instanceof Boolean;
  }

  static boolean validateBooleanIfPresent(Object o) {
    return o == null || o instanceof Boolean;
  }

  static boolean validateIntegerIfPresent(Object o) {
    return o == null || o instanceof Integer;
  }

  static boolean validateIntegerWithRangeIfPresent(Object o, int startValue, int endValue) {
    return o == null
        || (o instanceof Integer && ((Integer) o > startValue && (Integer) o < endValue));
  }

  @SuppressWarnings("unused")
  static boolean validateUuid(Object o) {
    try {
      UUID uuid = UUID.fromString((String) o);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  static boolean validateUuidIfPresent(String o) {
    return o == null || validateUuid(o);
  }

  static boolean validateLanguageIfPresent(Object o) {
    try {
      return o == null || LanguageValidator.isValidLanguage((Integer) o);
    } catch (ClassCastException e) {
      LOGGER.warn("Passed language id is not of Long type");
      return false;
    }
  }

  boolean validateField(Object value);
}
