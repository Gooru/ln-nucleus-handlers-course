package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 29/12/16.
 */
class CourseCreateProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CourseCreateProcessor.class);

  public CourseCreateProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // no op
  }

  @Override
  protected MessageResponse processCommand() {
    try {
      LOGGER.info("Creating new course");
      return new RepoBuilder().buildCourseRepo(context).createCourse();
    } catch (Throwable t) {
      LOGGER.error("Exception while creating course", t.getMessage());
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }

  }
}
