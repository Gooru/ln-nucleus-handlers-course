package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public final class Validators {

  private static final Logger LOGGER = LoggerFactory.getLogger(Validators.class);

  private Validators() {
    throw new AssertionError();
  }

  public static void validateCourseInContext(ProcessorContext context) {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id for fetch course");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid course id provided to fetch course"));
    }
  }

  public static void validateUserIdInContext(ProcessorContext context) {
    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user id to fetch course");
      throw new MessageResponseWrapperException(MessageResponseFactory.createForbiddenResponse());
    }
  }

  public static void validateFWInContext(ProcessorContext context) {
    if (context.frameworkCode() == null || context.frameworkCode().isEmpty()) {
      LOGGER.warn("Invalid user id to fetch course");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInvalidRequestResponse("Invalid framework code"));
    }
  }

}
