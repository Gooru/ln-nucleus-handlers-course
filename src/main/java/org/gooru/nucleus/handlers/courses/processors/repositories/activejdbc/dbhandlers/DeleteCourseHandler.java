package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class DeleteCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCourseHandler.class);
  private final ProcessorContext context;

  public DeleteCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id for delete");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for delete"), ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to delete course");
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
        LOGGER.warn("course {} is already deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course your are trying to delete is already deleted"),
                ExecutionStatus.FAILED);
      }

      // Only owner can delete the course
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.warn("user is anonymous or not owner of course for delete. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("course {} not found to delete, aborting", context.courseId());
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
      ajEntityCourse.setString(AJEntityCourse.MODIFIER_ID, context.userId());

      if (ajEntityCourse.save()) {
        LOGGER.info("course {} marked as deleted successfully", context.courseId());

        // Update rest of the hierarchy of the course to deleted
        AJEntityUnit.update("is_deleted = ?, modifier_id = ?", "course_id = ?", true, context.userId(), context.courseId());
        AJEntityLesson.update("is_deleted = ?, modifier_id = ?", "course_id = ?", true, context.userId(), context.courseId());
        AJEntityCollection.update("is_deleted = ?, modifier_id = ?", "course_id = ?", true, context.userId(), context.courseId());
        // TODO: update modifier id
        AJEntityContent.update("is_deleted = ?", "course_id = ?", true, context.courseId());

        return new ExecutionResult<>(MessageResponseFactory.createDeleteResponse(), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("error in delete course");
        if (ajEntityCourse.hasErrors()) {
          Map<String, String> errMap = ajEntityCourse.errors();
          JsonObject errors = new JsonObject();
          errMap.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
        } else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error in deleting course"), ExecutionStatus.FAILED);
        }
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
