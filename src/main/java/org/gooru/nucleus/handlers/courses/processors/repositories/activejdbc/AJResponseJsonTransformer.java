package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AJResponseJsonTransformer {
  
  public JsonObject transform(String ajResult) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }
    
    for (String fieldName: CourseRepo.JSON_OBJECT_FIELDS) {
      String valueToXform = result.getString(fieldName);
      if (valueToXform != null && !valueToXform.isEmpty()) {
        JsonObject xformedValue = new JsonObject(valueToXform);
        result.remove(fieldName);
        result.put(fieldName, xformedValue);
      }
    }
    
    for (String fieldName: CourseRepo.JSON_ARRAY_FIELDS) {
      String valueToXform = result.getString(fieldName);
      if (valueToXform != null && !valueToXform.isEmpty()) {
        JsonArray xformedValue = new JsonArray(valueToXform);
        result.remove(fieldName);
        result.put(fieldName, xformedValue);
      }
    }
    return result;
  }
}
