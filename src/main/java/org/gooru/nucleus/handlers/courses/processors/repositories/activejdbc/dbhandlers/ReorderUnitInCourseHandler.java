package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.List;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ReorderUnitInCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReorderUnitInCourseHandler.class);
  private final ProcessorContext context;

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
    
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted, hence can't reorder units. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which you are trying to reorder units"),
                ExecutionStatus.FAILED);
      }

      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        if (!new JsonArray(ajEntityCourse.get(0).getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
          LOGGER.warn("user is not owner or collaborator of course to reoder units. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.warn("course {} not found to reorder units, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonArray unitsToReorder = context.request().getJsonArray("order");
    for (Object object : unitsToReorder) {
      JsonObject jsonObject = (JsonObject) object;
      String unitId = jsonObject.getValue("id").toString();
      Integer sequence = jsonObject.getInteger("sequence");
      LOGGER.info("Id {} -- Sequence {}", unitId, sequence);

      //check whether the unit exists, not deleted and associated with the course
      LazyList<AJEntityUnit> units = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, unitId);
      if(!units.isEmpty()) {
        AJEntityUnit unit = units.get(0);
        if(unit.getBoolean(AJEntityUnit.IS_DELETED)) {
          LOGGER.debug("unit {} is deleted so can not reorder", unitId);
          return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(unitId + " is deleted"),
                  ExecutionStatus.FAILED);
        }
        
        if(!unit.getString(AJEntityUnit.COURSE_ID).equalsIgnoreCase(context.courseId())) {
          LOGGER.debug("unit {} is not associated with course {}", unitId, context.courseId());
          return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Unit is not associated with course"),
                  ExecutionStatus.FAILED);
        }
        
        unit.setInteger(AJEntityUnit.SEQUENCE_ID, sequence);
        unit.setModifierId(context.userId());
        
        if (unit.hasErrors()) {
          LOGGER.debug("reordering unit {} has error", unitId);
          
        }
      } else {
        LOGGER.error("no matching unit found for id {}", unitId);
        return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Id provided to reorder units not exist in database"),
                ExecutionStatus.FAILED);
      }
    }
    
    return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
