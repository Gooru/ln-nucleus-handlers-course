package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.CourseEntityConstants;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteCourseHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCourseHandler.class);

  public DeleteCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id for delete");
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for delete"),
              ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    if (!AJEntityCourse.exists(context.courseId())) {
      LOGGER.info("course {} not found to delete, aborting", context.courseId());
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
      ajEntityCourse.set(CourseEntityConstants.IS_DELETED, true);
      if (ajEntityCourse.save()) {
        LOGGER.info("course marked as deleted successfully");
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createDeleteResponse(), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.info("error in delete course, returning errors");
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createValidationErrorResponse(ajEntityCourse.errors()),
                ExecutionStatus.FAILED);
      }
    } catch (Throwable t) {
      LOGGER.error("exception while delete course", t);
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
