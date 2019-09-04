package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;

public final class RequestQueryParamReader {

  private RequestQueryParamReader() {
    throw new AssertionError();
  }

  public static String readQueryParamAsString(JsonObject request, String paramName) {
    if (request == null || request.isEmpty() || paramName == null || paramName.isEmpty()) {
      return null;
    }

    JsonArray values = request.getJsonArray(paramName);
    if (values == null || values.isEmpty()) {
      return null;
    }

    String value = values.getString(0);
    return (value != null && !value.isEmpty()) ? value : null;
  }


  public static Integer readQueryParamAsInt(JsonObject request, String paramName) {
    try {
      String strValue = readQueryParamAsString(request, paramName);
      return strValue != null ? Integer.valueOf(strValue) : null;
    } catch (NumberFormatException nfe) {
      return null;
    }
  }

  public static UUID readQueryParamAsUuid(JsonObject request, String paramName) {
    try {
      String strValue = readQueryParamAsString(request, paramName);
      return strValue != null ? UUID.fromString(strValue) : null;
    } catch (IllegalArgumentException iae) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInvalidRequestResponse(iae.getMessage()));
    }
  }
}
