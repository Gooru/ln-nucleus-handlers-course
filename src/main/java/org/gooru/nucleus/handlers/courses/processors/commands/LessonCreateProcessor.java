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
class LessonCreateProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRemoveProcessor.class);

  public LessonCreateProcessor(ProcessorContext context) {
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
        LOGGER.error("Course id not available to create lesson. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }

      if (!ValidationUtils.validateId(context.unitId())) {
        LOGGER.error("Unit id not available to create lesson. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
      }

      LOGGER.info("creating lesson for unit {} of course {}", context.unitId(), context.courseId());
      return new RepoBuilder().buildLessonRepo(context).createLesson();
    } catch (Throwable t) {
      LOGGER.error("Exception while creating lesson", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }

  }
}
