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

public class ReorderUnitInCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReorderUnitInCourseHandler.class);
  private final ProcessorContext context;
  private List<AJEntityUnit> unitsToReorder;

  public ReorderUnitInCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to reorder units"),
              ExecutionStatus.FAILED);
    }
    
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to reorder units"),
              ExecutionStatus.FAILED);
    }
    
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }
    
    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    unitsToReorder = new ArrayList<>();
    try {
      List<String> invalidFields = new ArrayList<>();
      List<String> notNullFields = new ArrayList<>();

      String mapValue;
      for (Map.Entry<String, Object> entry : context.request()) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          LOGGER.debug("entry {} -- {}", entry.getKey(), mapValue);
          /*if (AJEntityCourse.UPDATABLE_FIELDS.contains(entry.getKey())) {
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
          }*/
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

    } catch (Exception e) {
      
    }
    
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted, hence can't reorder units. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which you are trying to reorder units"),
                ExecutionStatus.FAILED);
      }

      // TODO: check whether user is either owner or collaborator
    } else {
      LOGGER.warn("course {} not found to reorder units, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
