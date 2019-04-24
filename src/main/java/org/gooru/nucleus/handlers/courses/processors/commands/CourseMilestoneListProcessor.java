package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */
class CourseMilestoneListProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CourseMilestoneListProcessor.class);

  CourseMilestoneListProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // no op
  }

  @Override
  protected MessageResponse processCommand() {
    if (!ValidationUtils.validateId(context.courseId())) {
      LOGGER.error("Invalid request, course id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
    }

    LOGGER.info("Getting course '{}' with milestone pivot", context.courseId());
    return new RepoBuilder().buildMilestoneRepo(context).fetchCourseWithMilestones();

  }
}
