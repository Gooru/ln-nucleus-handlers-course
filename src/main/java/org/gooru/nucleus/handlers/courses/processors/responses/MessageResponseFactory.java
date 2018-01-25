package org.gooru.nucleus.handlers.courses.processors.responses;

import org.gooru.nucleus.handlers.courses.constants.HttpConstants;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilder;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 6/1/16.
 */
public class MessageResponseFactory {

    private static final String API_VERSION_DEPRECATED = "API version is deprecated";
    private static final String API_VERSION_NOT_SUPPORTED = "API version is not supported";

    public static MessageResponse createInvalidRequestResponse() {
        return new MessageResponse.Builder().failed().setStatusBadRequest().build();
    }

    public static MessageResponse createInvalidRequestResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusBadRequest().setContentTypeJson()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createForbiddenResponse() {
        return new MessageResponse.Builder().failed().setStatusForbidden().build();
    }

    public static MessageResponse createForbiddenResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusForbidden()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, message)).build();
    }

    public static MessageResponse createInternalErrorResponse() {
        return new MessageResponse.Builder().failed().setStatusInternalError().build();
    }

    public static MessageResponse createInternalErrorResponse(String message) {
        return new MessageResponse.Builder().failed().setStatusInternalError().setContentTypeJson()
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
        return new MessageResponse.Builder().successful().setStatusOkay().setContentTypeJson()
            .setResponseBody(responseBody).build();
    }

    public static MessageResponse createPostResponse(String location) {
        return new MessageResponse.Builder().successful().setHeader("Location", location).setStatusCreated().build();
    }

    public static MessageResponse createPostResponse(String location, EventBuilder eventBuilder) {
        return new MessageResponse.Builder().successful().setHeader("Location", location).setStatusCreated()
            .setEventData(eventBuilder.build()).build();
    }

    public static MessageResponse createNoContentResponse() {
        return new MessageResponse.Builder().successful().setStatusNoOutput().build();
    }

    public static MessageResponse createNoContentResponse(EventBuilder eventBuilder) {
        return new MessageResponse.Builder().successful().setStatusNoOutput().setEventData(eventBuilder.build())
            .build();
    }
    
    public static MessageResponse createNoContentResponse(EventBuilder eventBuilder,
        JsonArray tagsToAggregate) {
        return new MessageResponse.Builder().successful().setStatusNoOutput().setEventData(eventBuilder.build())
            .setTagsToAggregate(tagsToAggregate).build();
    }

    public static MessageResponse createValidationErrorResponse(JsonObject errors) {
        return new MessageResponse.Builder().validationFailed().setStatusBadRequest().setContentTypeJson()
            .setResponseBody(errors).build();
    }

    public static MessageResponse createVersionDeprecatedResponse() {
        return new MessageResponse.Builder().failed().setStatusHttpCode(HttpConstants.HttpStatus.GONE)
            .setContentTypeJson()
            .setResponseBody(new JsonObject().put(MessageConstants.MSG_MESSAGE, API_VERSION_DEPRECATED)).build();
    }
}
