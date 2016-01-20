package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCourseHandler.class);
  private final ProcessorContext context;

  public DeleteCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id for delete");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for delete"),
        ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    String sql = "SELECT " + AJEntityCourse.IS_DELETED + " FROM course WHERE " + AJEntityCourse.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());
    
    if (!ajEntityCourse.isEmpty()) {
      //irrespective of size, always get first 
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.info("course {} is already deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course your are trying to delete is already deleted"),
          ExecutionStatus.FAILED);
      }
      
      //check whether user is owner, if anonymous or not owner, send unauthorized back;
      if(!ajEntityCourse.get(0).getString(AJEntityCourse.CREATOR_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.info("user is anonymous or not owner of course for delete. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.info("course {} not found to delete, aborting", context.courseId());
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
      ajEntityCourse.set(AJEntityCourse.IS_DELETED, true);
      if (ajEntityCourse.save()) {
        LOGGER.info("course marked as deleted successfully");
        return new ExecutionResult<>(MessageResponseFactory.createDeleteResponse(), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.info("error in delete course, returning errors");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(ajEntityCourse.errors()),
          ExecutionStatus.FAILED);
      }
    } catch (Throwable t) {
      LOGGER.error("exception while delete course", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
