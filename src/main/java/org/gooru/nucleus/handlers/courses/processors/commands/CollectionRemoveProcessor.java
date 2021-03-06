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
class CollectionRemoveProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRemoveProcessor.class);

  public CollectionRemoveProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // no op
  }

  @Override
  protected MessageResponse processCommand() {

    if (!ValidationUtils.validateId(context.courseId())) {
      LOGGER.error("Course id not available to remove collection. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
    }

    if (!ValidationUtils.validateId(context.unitId())) {
      LOGGER.error("Unit id not available to remove collection. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
    }

    if (!ValidationUtils.validateId(context.lessonId())) {
      LOGGER.error("Lesson id not available to remove collection. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
    }

    if (!ValidationUtils.validateId(context.collectionId())) {
      LOGGER.error("Collection id not available to remove collection. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }

    return new RepoBuilder().buildLessonRepo(context).removeCollectionFromLesson();
  }
}
