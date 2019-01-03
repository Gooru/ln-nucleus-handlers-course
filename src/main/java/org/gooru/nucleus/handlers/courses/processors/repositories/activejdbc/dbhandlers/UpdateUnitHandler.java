package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class UpdateUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUnitHandler.class);
  private AJEntityUnit unitToUpdate;

  public UpdateUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to update unit");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid course id provided to update unit"),
          ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to update unit");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid unit id provided to update unit"),
          ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to update unit");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid data provided to update unit"),
          ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to update unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityUnit.editFieldSelector(),
            AJEntityUnit.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
        LOGGER.warn("Validation errors for request");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse
        .findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE,
            context.courseId(), false, context.userId(), context.userId());
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of course to create unit. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit
        .findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE,
            context.unitId(), context.courseId(), false);
    if (ajEntityUnit.isEmpty()) {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    unitToUpdate = new AJEntityUnit();
    unitToUpdate.setAllFromJson(context.request());
    unitToUpdate.setUnitId(context.unitId());
    unitToUpdate.setModifierId(context.userId());

    if (unitToUpdate.hasErrors()) {
      LOGGER.warn("updating unit has errors");
      return new ExecutionResult<>(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
          ExecutionStatus.FAILED);
    }

    if (unitToUpdate.isValid()) {
      if (unitToUpdate.save()) {
        LOGGER.info("unit {} updated successfully", context.unitId());
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(
                EventBuilderFactory.getUpdateUnitEventBuilder(context.unitId())),
            ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("error while saving udpated unit");
        return new ExecutionResult<>(
            MessageResponseFactory.createInternalErrorResponse("Error while updating unit"),
            ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("validation error while updating unit");
      return new ExecutionResult<>(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
          ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.unitToUpdate.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }
  
  private static class DefaultPayloadValidator implements PayloadValidator {
  }
}
