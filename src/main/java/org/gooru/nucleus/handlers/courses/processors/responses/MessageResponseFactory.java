package org.gooru.nucleus.handlers.courses.processors.responses;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.javalite.activejdbc.Errors;

import java.util.Set;

/**
 * Created by ashish on 6/1/16.
 */
public class MessageResponseFactory {
  public static MessageResponse createInvalidRequestResponse() {
    return new MessageResponse.Builder().failed().setStatusBadRequest().build();
  }

  public static MessageResponse createInvalidRequestResponse(String message) {
    return new MessageResponse.Builder().failed().setStatusBadRequest().setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message))
                                        .build();
  }

  public static MessageResponse createForbiddenResponse() {
    return new MessageResponse.Builder().failed().setStatusForbidden().build();
  }

  public static MessageResponse createInternalErrorResponse() {
    return new MessageResponse.Builder().failed().setStatusInternalError().build();
  }

  public static MessageResponse createInternalErrorResponse(String message) {
    return new MessageResponse.Builder().failed().setStatusInternalError()
                                        .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
  }

  public static MessageResponse createNotFoundResponse() {
    return new MessageResponse.Builder().failed().setStatusNotFound().build();
  }

  public static MessageResponse createNotFoundResponse(String message) {
    return new MessageResponse.Builder().failed().setStatusNotFound().setContentTypeJson()
                                        .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
  }

  public static MessageResponse createGetResponse(JsonObject responseBody) {
    return new MessageResponse.Builder().successful().setStatusOkay().setContentTypeJson().setResponseBody(responseBody).build();
  }

  public static MessageResponse createPostResponse(String location) {
    return new MessageResponse.Builder().successful().setHeader("Location", location).setStatusCreated().build();
  }

  public static MessageResponse createPutResponse(String location) {
    return new MessageResponse.Builder().successful().setHeader("Location", location).setStatusNoOutput().build();
  }

  public static MessageResponse createDeleteResponse() {
    return new MessageResponse.Builder().successful().setStatusNoOutput().build();
  }

  public static MessageResponse createValidationErrorResponse(Errors errors) {
    JsonObject errJson = new JsonObject();
    Set<String> errKeys = errors.keySet();
    for (String key : errKeys) {
      errJson.put(key, errors.get(key));
    }

    return new MessageResponse.Builder().validationFailed().setStatusNoOutput().setContentTypeJson().setResponseBody(errJson).build();
  }
}
