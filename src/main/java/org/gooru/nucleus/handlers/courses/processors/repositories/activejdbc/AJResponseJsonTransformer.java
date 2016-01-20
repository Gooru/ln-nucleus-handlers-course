package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AJResponseJsonTransformer {

  public JsonObject transformCourse(String ajResult) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }

    String mapValue;
    for (Map.Entry<String, Object> entry : result) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      if (mapValue != null && !mapValue.isEmpty()) {
        if (Arrays.asList(AJEntityCourse.JSON_OBJECT_FIELDS).contains(entry.getKey())) {
          result.put(entry.getKey(), new JsonObject(mapValue));
        } else if (Arrays.asList(AJEntityCourse.JSON_ARRAY_FIELDS).contains(entry.getKey())) {
          result.put(entry.getKey(), new JsonArray(mapValue));
        }
      }
    }

    return result;
  }
  
  public JsonObject transformUnit(String ajResult) {
    JsonObject result = new JsonObject(ajResult);
    if (ajResult == null || ajResult.isEmpty()) {
      return result;
    }

    String mapValue;
    for (Map.Entry<String, Object> entry : result) {
      mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
      if (mapValue != null && !mapValue.isEmpty()) {
        if (Arrays.asList(AJEntityUnit.JSON_OBJECT_FIELDS).contains(entry.getKey())) {
          result.put(entry.getKey(), new JsonObject(mapValue));
        } 
      }
    }

    return result;
    
  }
  
  public JsonArray transformUnitSummary(String ajUnitSummary) {
    JsonArray result = new JsonArray(ajUnitSummary);
    if(ajUnitSummary == null || ajUnitSummary.isEmpty()) {
      return result;
    }
    
    JsonArray toReturn = new JsonArray();
    Iterator<Object> iterator = result.iterator();
    while(iterator.hasNext()) {
      JsonObject jsonObj = (JsonObject) (iterator.next());
      toReturn.add(jsonObj);
    }
    
    return toReturn;
  }
}
