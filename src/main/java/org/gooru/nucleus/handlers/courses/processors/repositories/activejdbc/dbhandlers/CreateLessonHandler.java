package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class CreateLessonHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLessonHandler.class);

  public CreateLessonHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to create lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to create lesson"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to create lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to create lesson"),
              ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to create lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create lesson"),
              ExecutionStatus.FAILED);
    }
    
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to create lesson");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    JsonObject request = context.request();
    List<String> missingFields = new ArrayList<>();
    for (String fieldName : AJEntityLesson.NOTNULL_FIELDS) {
      if (request.getString(fieldName) == null || request.getString(fieldName).isEmpty()) {
        missingFields.add(fieldName);
      }
    }

    if (missingFields.size() > 0) {
      LOGGER.warn("request data validation failed for '{}'", String.join(",", missingFields));
      return new ExecutionResult<>(
              MessageResponseFactory.createValidationErrorResponse(new JsonObject().put("missingFields", String.join(",", missingFields))),
              ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted, hence can't create lesson. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which you are trying to create lesson"),
                ExecutionStatus.FAILED);
      }

      //TODO: check whether user is owner or collaborator
    } else {
      LOGGER.warn("course {} not found to create lesson, aborting", context.courseId());
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

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject request = context.request();
    AJEntityLesson newLesson = new AJEntityLesson();
    String mapValue;
    try {
      for (Map.Entry<String, Object> entry : request) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (AJEntityLesson.JSON_FIELDS.contains(entry.getKey())) {
            PGobject jsonbField = new PGobject();
            jsonbField.setType("jsonb");
            jsonbField.setValue(mapValue);
            newLesson.set(entry.getKey(), jsonbField);
          } else {
            newLesson.set(entry.getKey(), entry.getValue());
          }
        }
      }

      String id = UUID.randomUUID().toString();
      boolean isDuplicate = true;
      while (isDuplicate) {
        if (AJEntityCourse.exists(id)) {
          id = UUID.randomUUID().toString();
        } else {
          isDuplicate = false;
        }
      }

      newLesson.setId(id);
      newLesson.set(AJEntityLesson.COURSE_ID, context.courseId());
      newLesson.set(AJEntityLesson.UNIT_ID, context.unitId());
      newLesson.set(AJEntityLesson.CREATOR_ID, context.userId());
      newLesson.set(AJEntityLesson.MODIFIER_ID, context.userId());
      newLesson.set(AJEntityLesson.ORIGINAL_CREATOR_ID, context.userId());
      newLesson.set(AJEntityLesson.IS_DELETED, false);

      
      Object maxSequenceId = Base.firstCell(AJEntityLesson.SELECT_LESSON_MAX_SEQUENCEID, context.courseId(), context.unitId());
      int sequenceId = 1;
      if (maxSequenceId != null) {
        sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;

      }
      newLesson.set(AJEntityLesson.SEQUENCE_ID, sequenceId);

      if (newLesson.isValid()) {
        if (newLesson.insert()) {
          LOGGER.info("lesson {} created successfully for unit {}", id, context.unitId());
          return new ExecutionResult<>(MessageResponseFactory.createPostResponse(id), ExecutionStatus.SUCCESSFUL);
        } else {
          throw new Exception("Something went wrong, unable to create lesson. Try Again!");
        }
      } else {
        LOGGER.error("Error while creating lesson");
        if (newLesson.hasErrors()) {
          Map<String, String> errMap = newLesson.errors();
          JsonObject errors = new JsonObject();
          errMap.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
        } else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error while creating lesson"), ExecutionStatus.FAILED);
        }
      }
    } catch (Throwable t) {
      LOGGER.error("Exception while creating lesson", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
