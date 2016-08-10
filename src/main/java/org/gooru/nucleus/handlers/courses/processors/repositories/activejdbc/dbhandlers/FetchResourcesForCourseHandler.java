package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FetchResourcesForCourseHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchResourcesForCourseHandler.class);
    private String taxonomy;

    public FetchResourcesForCourseHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id to fetch lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch lesson"),
                ExecutionStatus.FAILED);
        }

        taxonomy = readRequestParam(MessageConstants.TAXONOMY);
        if (taxonomy == null) {
            LOGGER.warn("invalid learning target or standard provided in request");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
                "Invalid learning target or standard provided in request"), ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityCourse> ajEntityCourse =
            AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
        if (ajEntityCourse.isEmpty()) {
            LOGGER.warn("course {} not found to fetch lesson, aborting", context.courseId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("validateRequest() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        LazyList<AJEntityContent> resourceList =
            AJEntityContent.findBySQL(AJEntityContent.SELECT_RESOURCES_BY_COURSE, context.courseId(), taxonomy);
        JsonArray resourceArray = new JsonArray();
        resourceList.stream().forEach(resource -> resourceArray.add(new JsonObject(new JsonFormatterBuilder()
            .buildSimpleJsonFormatter(false, AJEntityContent.RESOURCES_BY_COURSE_FIELDS).toJson(resource))));

        JsonObject responseBody = new JsonObject();
        responseBody.put(MessageConstants.RESP_JSON_KEY_RESOURCES, resourceArray);

        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(responseBody),
            ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private String readRequestParam(String param) {
        JsonArray requestParams = context.request().getJsonArray(param);
        if (requestParams == null || requestParams.isEmpty()) {
            return null;
        }

        String value = requestParams.getString(0);
        return (value != null && !value.isEmpty()) ? value : null;
    }

}
