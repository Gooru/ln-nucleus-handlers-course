package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CreateUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateUnitHandler.class);

  public CreateUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {

    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to delete unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to delete unit"),
              ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to create unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create unit"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to create unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    JsonObject request = context.request();
    List<String> missingFields = new ArrayList<>();
    for (String fieldName : AJEntityUnit.NOTNULL_FIELDS) {
      if (request.getString(fieldName) == null || request.getString(fieldName).isEmpty()) {
        missingFields.add(fieldName);
      }
    }

    // TODO: May be need to revisit this logic of validating fields and
    // returning error back for all validation failed in one go
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
        LOGGER.warn("course {} is deleted, hence can't create unit. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which you are trying to create unit"),
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
      LOGGER.warn("course {} not found to create unit, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject request = context.request();
    AJEntityUnit newUnit = new AJEntityUnit();
    String mapValue;
    try {
      for (Map.Entry<String, Object> entry : request) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (AJEntityUnit.JSON_FIELDS.contains(entry.getKey())) {
            PGobject jsonbField = new PGobject();
            jsonbField.setType("jsonb");
            jsonbField.setValue(mapValue);
            newUnit.set(entry.getKey(), jsonbField);
          } else {
            newUnit.set(entry.getKey(), entry.getValue());
          }
        }
      }

      // TODO: UUID should be generated from separate utility
      // Check for duplicate id, if its already exists in same table, generate
      // new
      // Probably need to revisit this logic again or need to move in separate
      // utility
      String id = UUID.randomUUID().toString();
      boolean isDuplicate = true;
      while (isDuplicate) {
        if (AJEntityCourse.exists(id)) {
          id = UUID.randomUUID().toString();
        } else {
          isDuplicate = false;
        }
      }

      newUnit.setId(id);
      newUnit.set(AJEntityUnit.COURSE_ID, context.courseId());
      newUnit.set(AJEntityUnit.OWNER_ID, context.userId());
      newUnit.set(AJEntityUnit.CREATOR_ID, context.userId());
      newUnit.set(AJEntityUnit.MODIFIER_ID, context.userId());
      newUnit.set(AJEntityUnit.ORIGINAL_CREATOR_ID, context.userId());
      newUnit.set(AJEntityUnit.IS_DELETED, false);

      // Get max sequence id for course
      Object maxSequenceId = Base.firstCell(AJEntityUnit.SELECT_UNIT_MAX_SEQUENCEID, context.courseId());
      int sequenceId = 1;
      if (maxSequenceId != null) {
        sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;

      }
      newUnit.set(AJEntityUnit.SEQUENCE_ID, sequenceId);

      if (newUnit.isValid()) {
        if (newUnit.insert()) {
          LOGGER.info("unit {} created successfully for course {}", id, context.courseId());
          return new ExecutionResult<>(MessageResponseFactory.createPostResponse(id), ExecutionStatus.SUCCESSFUL);
        } else {
          throw new Exception("Something went wrong, unable to create unit. Try Again!");
        }
      } else {
        LOGGER.error("Error while creating unit");
        if (newUnit.hasErrors()) {
          Map<String, String> errMap = newUnit.errors();
          JsonObject errors = new JsonObject();
          errMap.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
        } else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error while creating unit"), ExecutionStatus.FAILED);
        }
      }
    } catch (Throwable t) {
      LOGGER.error("Exception while creating unit", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
