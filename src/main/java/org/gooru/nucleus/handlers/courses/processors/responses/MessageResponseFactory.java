package org.gooru.nucleus.handlers.courses.processors.responses;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;

import io.vertx.core.json.JsonObject;

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
  
  public static MessageResponse createGetResponse(JsonObject responseBody) {
    return new MessageResponse.Builder().successful().setStatusOkay().setContentTypeJson().setResponseBody(responseBody).build();
  }
  
  public static MessageResponse createPostResponse(String location) {
    return new MessageResponse.Builder().successful().setHeader("Location", location).setStatusCreated().build();
  }
}
