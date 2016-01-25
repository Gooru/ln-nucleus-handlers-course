package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
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

public class UpdateLessonHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLessonHandler.class);
  private AJEntityLesson lessonToUpdate = null;

  public UpdateLessonHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to update lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to update lesson"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to update lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to update lesson"),
              ExecutionStatus.FAILED);
    }

    if (context.lessonId() == null || context.lessonId().isEmpty()) {
      LOGGER.warn("invalid lesson id to update lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id provided to update lesson"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to update lesson");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted, hence can't update lesson. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which you are trying to update lesson"),
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
      LOGGER.warn("course {} not found to update lesson, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId());
    if (!ajEntityUnit.isEmpty()) {
      if (ajEntityUnit.get(0).getBoolean(AJEntityUnit.IS_DELETED)) {
        LOGGER.warn("unit {} is deleted. Aborting", context.unitId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Unit is deleted"), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityLesson> ajEntityLesson = AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, context.lessonId());
    if (!ajEntityLesson.isEmpty()) {
      if (ajEntityLesson.get(0).getBoolean(AJEntityLesson.IS_DELETED)) {
        LOGGER.warn("Lesson {} is deleted, aborting.", context.lessonId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Lesson is deleted"), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("Lesson {} not found, aborting", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
    
    lessonToUpdate = new AJEntityLesson();
    try {
      List<String> invalidFields = new ArrayList<>();
      List<String> notNullFields = new ArrayList<>();

      String mapValue;
      for (Map.Entry<String, Object> entry : context.request()) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (AJEntityLesson.UPDATABLE_FIELDS.contains(entry.getKey())) {
            if (AJEntityLesson.JSON_FIELDS.contains(entry.getKey())) {
              PGobject jsonbField = new PGobject();
              jsonbField.setType("jsonb");
              jsonbField.setValue(mapValue);
              lessonToUpdate.set(entry.getKey(), jsonbField);
            } else {
              lessonToUpdate.set(entry.getKey(), entry.getValue());
            }
          } else {
            invalidFields.add(entry.getKey());
          }
        } else {
          if (AJEntityLesson.NOTNULL_FIELDS.contains(entry.getKey())) {
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
    lessonToUpdate.setId(context.lessonId());
    lessonToUpdate.setString(AJEntityLesson.MODIFIER_ID, context.userId());

    if (lessonToUpdate.save()) {
      LOGGER.info("lesson {} updated successfully", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.lessonId()), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("error in updating lesson");
      if (lessonToUpdate.hasErrors()) {
        Map<String, String> errMap = lessonToUpdate.errors();
        JsonObject errors = new JsonObject();
        errMap.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
      } else {
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error while updating lesson"), ExecutionStatus.FAILED);
      }
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
