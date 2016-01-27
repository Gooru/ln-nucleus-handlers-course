package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

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

import io.vertx.core.json.JsonObject;

public class UpdateCollaboratorHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollaboratorHandler.class);
  private final ProcessorContext context;

  public UpdateCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {

    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id to update collaborator");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to udpate collaborator"),
              ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received, aborting");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided request"), ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to update course collaborator");
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
        LOGGER.warn("course {} is deleted, hence collborators can't be updated. Aborting", context.courseId());
        return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse("Course is deleted for which your are trying to update collaborators"),
                ExecutionStatus.FAILED);
      }

      // check whether user is owner of course
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.warn("user is not owner of course to udpate collaborator. aborting");
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
        if (ajEntityCourse.hasErrors()) {
          Map<String, String> errMap = ajEntityCourse.errors();
          JsonObject errors = new JsonObject();
          errMap.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
        } else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error in update collaborators of course"),
                  ExecutionStatus.FAILED);
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
