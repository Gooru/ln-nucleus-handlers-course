/**
 * 
 */
package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;

/**
 * @author Sachin
 *
 */
public class CourseValidator {

  private final ProcessorContext context;
  private final Logger LOGGER;

  public CourseValidator(ProcessorContext context, Logger logger) {
    this.context = context;
    this.LOGGER = logger;
  }

  public ExecutionResult<MessageResponse> checkIsDeleted() {
    String sql = "SELECT " + AJEntityCourse.IS_DELETED + " FROM course WHERE " + AJEntityCourse.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());

    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.size() >= 2) {
        // only log, if more than one course is found
        LOGGER.debug("more that 1 course found for id {}", context.courseId());
      }

      // irrespective of size, always get first
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.info("course {} is deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted"), ExecutionStatus.FAILED);
      }

    } else {
      LOGGER.info("course {} not found, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  public ExecutionResult<MessageResponse> checkIsDeletedAndOwner() {

    String sql = "SELECT " + AJEntityCourse.IS_DELETED + ", " + AJEntityCourse.CREATOR_ID + " FROM course WHERE "
            + AJEntityCourse.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());

    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.size() >= 2) {
        // only log, if more than one course is found
        LOGGER.debug("more that 1 course found for id {}", context.courseId());
      }

      // irrespective of size, always get first
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.info("course {} is deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted"), ExecutionStatus.FAILED);
      }

      // check whether user is owner, if anonymous or not owner, send
      // unauthorized back;
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.CREATOR_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.info("user is anonymous or not owner of course. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.info("course {} not found, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }
}
