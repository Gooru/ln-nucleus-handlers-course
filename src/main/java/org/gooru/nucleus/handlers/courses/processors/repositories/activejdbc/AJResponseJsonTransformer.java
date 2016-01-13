package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import java.util.Arrays;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AJResponseJsonTransformer {
  private static final Logger LOGGER = LoggerFactory.getLogger(AJResponseJsonTransformer.class);
  
  public JsonObject transform(String ajResult) {
    LOGGER.debug("received string to transform in json:" + ajResult);
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }
    
    String mapValue = null;
    for (Map.Entry<String, Object> entry : result) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      LOGGER.info("map value:" + mapValue);
      if(mapValue != null && !mapValue.isEmpty()) {
        if(Arrays.asList(CourseRepo.JSON_OBJECT_FIELDS).contains(entry.getKey())) {
          //result.remove(entry.getKey());
          result.put(entry.getKey(), new JsonObject(mapValue));
        } else if (Arrays.asList(CourseRepo.JSON_ARRAY_FIELDS).contains(entry.getKey())) {
          //result.remove(entry.getKey());
          result.put(entry.getKey(), new JsonArray(mapValue));
        } 
      }
    }
    
    /*
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
    }*/
    return result;
  }
}
