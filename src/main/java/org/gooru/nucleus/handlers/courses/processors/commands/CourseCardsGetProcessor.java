package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author subbu on 16 Oct 2018.
 */
class CourseCardsGetProcessor extends AbstractCommandProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CourseCardsGetProcessor.class);

  public CourseCardsGetProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // no op
  }

  @Override
  protected MessageResponse processCommand() {
    return new RepoBuilder().buildCourseRepo(context).fetchCourseCards();

  }
}
