package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 15-Mar-2017
 */
public class CollectionsByCourseProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsByCourseProcessor.class);

  protected CollectionsByCourseProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      if (!ValidationUtils.validateId(context.courseId())) {
        LOGGER.error("Invalid request, course id not available. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }

      LOGGER.info("getting collections by course {}", context.courseId());
      return new RepoBuilder().buildCourseRepo(context).fetchCollectionsByCourse();
    } catch (Throwable t) {
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

}
