package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author ashish.
 */

public final class Validators {

  private static final Logger LOGGER = LoggerFactory.getLogger(Validators.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private Validators() {
    throw new AssertionError();
  }

  public static void validateCourseInContext(ProcessorContext context) {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id recevied from context.");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.course.id")));
    }
  }
  
  public static void validateUnitInContext(ProcessorContext context) {
    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id recevied from context.");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.unit.id")));
    }
  }
  
  public static void validateLessonInContext(ProcessorContext context) {
    if (context.lessonId() == null || context.lessonId().isEmpty()) {
      LOGGER.warn("invalid lesson id recevied from context.");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.lesson.id")));
    }
  }
  
  public static void validateLessonPlanInContext(ProcessorContext context) {
    if (context.lessonPlanId() == null || context.lessonPlanId().isEmpty()) {
      LOGGER.warn("invalid lesson plan id recevied from context.");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.lesson.plan.id")));
    }
  }
  
  public static void validateUser(ProcessorContext context) {
    validateUser(context.userId(), true);
  }

  private static void validateUser(String userId, boolean allowAnonymous) {
    if ((userId == null) || userId.isEmpty() || (
    MessageConstants.MSG_USER_ANONYMOUS.equalsIgnoreCase(userId) && !allowAnonymous)) {
      LOGGER.warn("Invalid user");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")));
    }
  }
  
  public static void validatePayloadNotEmpty(JsonObject request) {
    if (request == null || request.isEmpty()) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")));
    }
  }
  
  public static void validateWithDefaultPayloadValidator(JsonObject request,
      FieldSelector fieldSelector, ValidatorRegistry validatorRegistry) {
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(request, fieldSelector, validatorRegistry);
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createValidationErrorResponse(errors));
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
  private static class DefaultPayloadValidator implements PayloadValidator {

  }
}
