package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
