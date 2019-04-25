package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
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
      LOGGER.warn("Invalid FW code to fetch course");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInvalidRequestResponse("Invalid framework code"));
    }
  }

  public static void validateMilestoneInContext(ProcessorContext context) {
    if (context.milestoneId() == null || context.milestoneId().isEmpty()) {
      LOGGER.warn("Invalid user id to fetch course");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInvalidRequestResponse("Invalid milestoneId"));
    }
  }

  public static void validateCourseIsPremium(AJEntityCourse course, ProcessorContext context) {
    if (!course.isPremium()) {
      LOGGER.warn("Course: '{}' is not premium", context.courseId());
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              "Milestone API is available for navigator courses only"));

    }
  }
}
