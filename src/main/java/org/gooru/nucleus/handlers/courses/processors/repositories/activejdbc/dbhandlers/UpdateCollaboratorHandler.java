package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCollaboratorHandler implements DBHandler {
  
  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollaboratorHandler.class);
  
  public UpdateCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }
  
  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.debug("invalid request received, aborting");
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided request"),
              ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    if(!AJEntityCourse.exists(context.courseId())) {
      LOGGER.debug("course {} not found to update, aborting", context.courseId());
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
    
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      AJEntityCourse ajEntityCourse = new AJEntityCourse();
      ajEntityCourse.setId(context.courseId());
      
      PGobject jsonbField = new PGobject();
      jsonbField.setType("jsonb");
      jsonbField.setValue(context.request().getJsonArray(CourseRepo.COLLABORATOR).toString());
        
      ajEntityCourse.set(CourseRepo.COLLABORATOR, jsonbField);
      if(ajEntityCourse.save()) {
        LOGGER.debug("updated course successfully");
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createPutReponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.debug("error in update course, returning errors");
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createValidationErrorResponse(ajEntityCourse.errors()), ExecutionStatus.FAILED);
      }
    } catch (Throwable t) {
      LOGGER.error("exception while updating course", t);
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
