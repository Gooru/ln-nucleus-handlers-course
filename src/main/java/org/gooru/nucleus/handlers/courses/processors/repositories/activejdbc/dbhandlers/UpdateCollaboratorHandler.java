package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;

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

import io.vertx.core.json.JsonObject;

public class UpdateCollaboratorHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollaboratorHandler.class);
  private final ProcessorContext context;

  public UpdateCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received, aborting");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided request"),
        ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    //Check whether the course is deleted or not, which will also verify if course exists or not
    String sql = "SELECT " + AJEntityCourse.IS_DELETED + ", " + AJEntityCourse.CREATOR_ID + " FROM course WHERE " + AJEntityCourse.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());

    if (!ajEntityCourse.isEmpty()) {

      //irrespective of size, always get first 
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted, hence collborators can't be updated. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which your are trying to update collaborators"),
          ExecutionStatus.FAILED);
      }
      
      //check whether user is owner, if anonymous or not owner, send unauthorized back;
      if(!ajEntityCourse.get(0).getString(AJEntityCourse.CREATOR_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.warn("user is anonymous or not owner of course to update collaborators. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("course {} not found to update collaborators, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      AJEntityCourse ajEntityCourse = new AJEntityCourse();
      ajEntityCourse.setId(context.courseId());
      ajEntityCourse.setString(AJEntityCourse.MODIFIER_ID, context.userId());

      PGobject jsonbField = new PGobject();
      jsonbField.setType("jsonb");
      jsonbField.setValue(context.request().getJsonArray(AJEntityCourse.COLLABORATOR).toString());

      ajEntityCourse.set(AJEntityCourse.COLLABORATOR, jsonbField);
      if (ajEntityCourse.save()) {
        LOGGER.info("updated collaborators of course {} successfully", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("error in update collaborators of course {}", context.courseId());
        if(ajEntityCourse.hasErrors()) {
          Map<String, String> errMap = ajEntityCourse.errors();
          JsonObject errors = new JsonObject();
          errMap.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionStatus.FAILED);
        } else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error in update collaborators of course"), ExecutionStatus.FAILED);
        }
      }
    } catch (Throwable t) {
      LOGGER.error("exception while updating course", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
