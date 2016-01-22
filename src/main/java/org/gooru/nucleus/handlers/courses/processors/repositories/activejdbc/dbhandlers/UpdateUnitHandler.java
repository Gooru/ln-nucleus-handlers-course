package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class UpdateUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUnitHandler.class);
  private AJEntityUnit unitToUpdate;

  public UpdateUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id to update unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to update unit"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.info("invalid unit id to update unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to update unit"),
              ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to update unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to update unit"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to update unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    unitToUpdate = new AJEntityUnit();
    try {
      List<String> invalidFields = new ArrayList<>();
      List<String> notNullFields = new ArrayList<>();

      String mapValue;
      for (Map.Entry<String, Object> entry : context.request()) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (AJEntityUnit.UPDATABLE_FIELDS.contains(entry.getKey())) {
            if (AJEntityUnit.JSON_FIELDS.contains(entry.getKey())) {
              PGobject jsonbField = new PGobject();
              jsonbField.setType("jsonb");
              jsonbField.setValue(mapValue);
              unitToUpdate.set(entry.getKey(), jsonbField);
            } else {
              unitToUpdate.set(entry.getKey(), entry.getValue());
            }
          } else {
            invalidFields.add(entry.getKey());
          }
        } else {
          if (AJEntityUnit.NOTNULL_FIELDS.contains(entry.getKey())) {
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
      LOGGER.error("Exception while updating unit", e.getMessage());
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted"), ExecutionStatus.FAILED);
      }
      
    //TODO: check whether user is either owner or collaborator of course
    } else {
      LOGGER.warn("course {} not found, aborting", context.courseId());
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

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    unitToUpdate.setId(context.unitId());
    unitToUpdate.setString(AJEntityUnit.MODIFIER_ID, context.userId());

    if (unitToUpdate.save()) {
      LOGGER.info("unit {} updated successfully", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.unitId()), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("error in updating unit");
      if (unitToUpdate.hasErrors()) {
        Map<String, String> errMap = unitToUpdate.errors();
        JsonObject errors = new JsonObject();
        errMap.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
      } else {
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error while updating unit"), ExecutionStatus.FAILED);
      }
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
