package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 29/12/16.
 */
class LessonDeleteProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(LessonDeleteProcessor.class);

  public LessonDeleteProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // no op
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      if (!ValidationUtils.validateId(context.courseId())) {
        LOGGER.error("Course id not available to delete lesson. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }

      if (!ValidationUtils.validateId(context.unitId())) {
        LOGGER.error("Unit id not available to delete lesson. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
      }

      if (!ValidationUtils.validateId(context.lessonId())) {
        LOGGER.error("Lesson id not available to delete lesson. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
      }

      LOGGER.info("deleting lesson {} of unit {}", context.lessonId(), context.unitId());
      return new RepoBuilder().buildLessonRepo(context).deleteLesson();
    } catch (Throwable t) {
      LOGGER.error("Exception while deleting lesson", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }

  }
}
