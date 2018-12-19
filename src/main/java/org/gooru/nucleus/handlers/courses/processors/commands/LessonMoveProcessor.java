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
class LessonMoveProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(LessonMoveProcessor.class);

  public LessonMoveProcessor(ProcessorContext context) {
    super(context);

  }

  @Override
  protected void setDeprecatedVersions() {
    // no op
  }

  @Override
  protected MessageResponse processCommand() {
    if (!ValidationUtils.validateId(context.courseId())) {
      LOGGER.error("Course id not available to move lesson. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
    }

    if (!ValidationUtils.validateId(context.unitId())) {
      LOGGER.error("Unit id not available to move lesson. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
    }

    return new RepoBuilder().buildUnitRepo(context).moveLessonToUnit();

  }
}
