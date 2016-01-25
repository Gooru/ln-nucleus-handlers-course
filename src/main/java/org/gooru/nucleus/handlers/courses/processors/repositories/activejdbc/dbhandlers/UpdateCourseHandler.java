package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UpdateCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCourseHandler.class);
  private final ProcessorContext context;
  private AJEntityCourse courseToUpdate;

  public UpdateCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id for update");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for update"), ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid data provided to update course {}", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to update course"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to update course");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.size() >= 2) {
        LOGGER.debug("more that 1 course found for id {}", context.courseId());
      }

      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.info("course {} is deleted, hence can't be updated. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course your are trying to update is deleted"),
                ExecutionStatus.FAILED);
      }

      // check whether user is either owner or collaborator
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        if (!new JsonArray(ajEntityCourse.get(0).getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
          LOGGER.warn("user is not owner or collaborator of course to create unit. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.info("course {} not found to update, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    courseToUpdate = new AJEntityCourse();
    try {
      List<String> invalidFields = new ArrayList<>();
      List<String> notNullFields = new ArrayList<>();

      String mapValue;
      for (Map.Entry<String, Object> entry : context.request()) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (AJEntityCourse.UPDATABLE_FIELDS.contains(entry.getKey())) {
            if (AJEntityCourse.JSON_FIELDS.contains(entry.getKey())) {
              PGobject jsonbField = new PGobject();
              jsonbField.setType("jsonb");
              jsonbField.setValue(mapValue);
              courseToUpdate.set(entry.getKey(), jsonbField);
            } else {
              courseToUpdate.set(entry.getKey(), entry.getValue());
            }
          } else {
            invalidFields.add(entry.getKey());
          }
        } else {
          if (AJEntityCourse.NOTNULL_FIELDS.contains(entry.getKey())) {
            notNullFields.add(entry.getKey());
          }
        }
      }

      if (invalidFields.size() > 0) {
        LOGGER.error("not updatable fields present in request : {}", String.join(",", invalidFields));
        return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(new JsonObject().put("invalidFields", String.join(",", invalidFields))),
                ExecutionStatus.FAILED);
      }

      if (notNullFields.size() > 0) {
        LOGGER.error("trying to update not null fields to null value : {}", String.join(",", notNullFields));
        return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(new JsonObject().put("notnullFields", String.join(",", notNullFields))),
                ExecutionStatus.FAILED);
      }
    } catch (SQLException e) {
      LOGGER.error("Exception while updating course", e.getMessage());
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionStatus.FAILED);
    }
    
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    courseToUpdate.setId(context.courseId());
    courseToUpdate.set(AJEntityCourse.MODIFIER_ID, context.userId());

    if (courseToUpdate.save()) {
      LOGGER.info("course {} updated successfully", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("error in updating course");
      if (courseToUpdate.hasErrors()) {
        Map<String, String> errMap = courseToUpdate.errors();
        JsonObject errors = new JsonObject();
        errMap.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
      } else {
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error while updating course"), ExecutionStatus.FAILED);
      }
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
