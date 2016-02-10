package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCourseHandler.class);
  private final ProcessorContext context;
  private AJEntityCourse course;

  public CreateCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to create course");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create course"),
        ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to create course");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    JsonObject validateErrors = validateFields();
    if (validateErrors != null && !validateErrors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(validateErrors), ExecutionResult.ExecutionStatus.FAILED);
    }

    JsonObject notNullErrors = validateNullFields();
    if (notNullErrors != null && !notNullErrors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(notNullErrors), ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    course = new AJEntityCourse();
    course.setAllFromJson(context.request());
    course.setOwnerId(context.userId());
    course.setCreatorId(context.userId());
    course.setModifierId(context.userId());

    if (course.hasErrors()) {
      LOGGER.warn("errors in course creation");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }

    if (course.isValid()) {
      if (course.insert()) {
        LOGGER.info("course created successfully : {}", course.getId().toString());
        return new ExecutionResult<>(MessageResponseFactory.createPostResponse(course.getId().toString()), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("error while saving course");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("validation error while creating course");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject validateFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    input.fieldNames().stream().filter(key -> !AJEntityCourse.INSERTABLE_FIELDS.contains(key)).forEach(key -> output.put(key, "Field not allowed"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject validateNullFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    AJEntityCourse.NOTNULL_FIELDS.stream()
                                 .filter(notNullField -> (input.getValue(notNullField) == null || input.getValue(notNullField).toString().isEmpty()))
                                 .forEach(notNullField -> output.put(notNullField, "Field should not be empty or null"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.course.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }
}
