package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.CourseEntityConstants;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class CreateCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCourseHandler.class);
  private final ProcessorContext context;

  public CreateCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.info("invalid request received to create course");
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create course"),
        ExecutionStatus.FAILED);
    }

    JsonObject request = context.request();
    StringBuffer missingFields = new StringBuffer();
    for (String fieldName : CourseEntityConstants.NOTNULL_FIELDS) {
      if (request.getString(fieldName) == null || request.getString(fieldName).isEmpty()) {
        missingFields.append(fieldName + " ");
      }
    }
    // TODO: May be need to revisit this logic of validating fields and
    // returning error back for all validation failed in one go
    if (!missingFields.toString().isEmpty()) {
      LOGGER.info("request data validation failed for '{}'", missingFields.toString());
      return new ExecutionResult<MessageResponse>(
        MessageResponseFactory.createInvalidRequestResponse("mandatory field(s) '" + missingFields.toString() + "' missing"),
        ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject request = context.request();
    AJEntityCourse course = new AJEntityCourse();
    String mapValue = null;
    try {
      for (Map.Entry<String, Object> entry : request) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (Arrays.asList(CourseEntityConstants.JSON_FIELDS).contains(entry.getKey())) {
            PGobject jsonbField = new PGobject();
            jsonbField.setType("jsonb");
            jsonbField.setValue(mapValue);
            course.set(entry.getKey(), jsonbField);
          } else {
            course.set(entry.getKey(), entry.getValue());
          }
        }
      }

      // TODO: UUID should be generated from separate utility
      // Check for duplicate id, if its already exists in same table, generate new
      // Probably need to revisit this logic again or need to move in separate utility
      String id = UUID.randomUUID().toString();
      boolean isDuplicate = true;
      while (isDuplicate) {
        if (AJEntityCourse.exists(id)) {
          id = UUID.randomUUID().toString();
        } else {
          isDuplicate = false;
        }
      }

      course.setId(id);
      course.set(CourseEntityConstants.CREATOR_ID, context.userId());
      course.set(CourseEntityConstants.ORIGINAL_CREATOR_ID, context.userId());

      if (course.isValid()) {
        if (course.insert()) {
          LOGGER.info("course created successfully : {}", id);
          return new ExecutionResult<MessageResponse>(MessageResponseFactory.createPostResponse(id), ExecutionStatus.SUCCESSFUL);
        } else {
          throw new Exception("Something went wrong, unable to save course. Try Again!");
        }
      } else {
        LOGGER.info("Error while creating course");
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createValidationErrorResponse(course.errors()), ExecutionStatus.FAILED);
      }
    } catch (Throwable t) {
      LOGGER.error("Exception while creating course", t);
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
