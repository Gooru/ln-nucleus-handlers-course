package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

  final String userId;
  final JsonObject prefs;
  final JsonObject request;
  final String courseId;

  public ProcessorContext(String userId, JsonObject prefs, JsonObject request, String courseId) {
    if (prefs == null || userId == null || prefs.isEmpty()) {
      throw new IllegalStateException("Processor Context creation failed because of invalid values");
    }
    this.userId = userId;
    this.prefs = prefs.copy();
    this.request = request != null ? request.copy() : null;
    this.courseId = courseId;
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

}
