package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

  private final String userId;
  private final JsonObject prefs;
  private final JsonObject request;
  private final String courseId;
  private final String unitId;
  private final String lessonId;

  public ProcessorContext(String userId, JsonObject prefs, JsonObject request, String courseId, String unitId, String lessonId) {
    if (prefs == null || userId == null || prefs.isEmpty()) {
      throw new IllegalStateException("Processor Context creation failed because of invalid values");
    }
    this.userId = userId;
    this.prefs = prefs.copy();
    this.request = request != null ? request.copy() : null;
    this.courseId = courseId;
    this.unitId = unitId;
    this.lessonId = lessonId;
  }

  public String userId() {
    return this.userId;
  }

  public JsonObject prefs() {
    return this.prefs.copy();
  }

  public JsonObject request() {
    return this.request;
  }

  public String courseId() {
    return this.courseId;
  }
  
  public String unitId() {
    return this.unitId;
  }
  
  public String lessonId() {
    return this.lessonId;
  }

}
