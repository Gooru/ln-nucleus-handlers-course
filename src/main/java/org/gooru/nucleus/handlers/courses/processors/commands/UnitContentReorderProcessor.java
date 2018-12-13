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
class UnitContentReorderProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnitContentReorderProcessor.class);

  public UnitContentReorderProcessor(ProcessorContext context) {
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
        LOGGER.error("Course id not available to reorder lessons. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }

      if (!ValidationUtils.validateId(context.unitId())) {
        LOGGER.error("Unit id not available to reorder lessons. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
      }

      LOGGER.info("reordering lessons for unit {} of course {}", context.unitId(),
          context.courseId());
      return new RepoBuilder().buildUnitRepo(context).reorderLessonInUnit();
    } catch (Throwable t) {
      LOGGER.error("Exception while reordering lessons", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }

  }
}
