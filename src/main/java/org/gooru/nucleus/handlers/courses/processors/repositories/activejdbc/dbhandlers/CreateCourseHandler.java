package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.settings.TenantSettingsProviderBuilder;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class CreateCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCourseHandler.class);
  private final ProcessorContext context;
  private AJEntityCourse course;

  public CreateCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to create course");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid data provided to create course"),
          ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to create course");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }
    
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityCourse.createFieldSelector(),
            AJEntityCourse.getValidatorRegistry());
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
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    course = new AJEntityCourse();
    autoPopulateFields();
    course.setAllFromJson(context.request());

    course.setInteger(AJEntityCourse.SEQUENCE_ID, getSequenceId());

    if (course.hasErrors()) {
      LOGGER.warn("errors in course creation");
      return new ExecutionResult<>(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
          ExecutionStatus.FAILED);
    }

    if (course.isValid()) {
      if (course.insert()) {
        LOGGER.info("course created successfully : {}", course.getId().toString());
        return new ExecutionResult<>(
            MessageResponseFactory.createPostResponse(course.getId().toString(),
                EventBuilderFactory.getCreateCourseEventBuilder(course.getId().toString())),
            ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("error while saving course");
        return new ExecutionResult<>(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
            ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("validation error while creating course");
      return new ExecutionResult<>(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
          ExecutionStatus.FAILED);
    }
  }

  private int getSequenceId() {
    // Get max sequence id of course for subject bucket
    Object maxSequenceId;
    int sequenceId = 1;
    String subjectBucket = course.getString(AJEntityCourse.SUBJECT_BUCKET);
    if (subjectBucket != null && !subjectBucket.isEmpty()) {
      maxSequenceId =
          Base.firstCell(AJEntityCourse.SELECT_MAX_SEQUENCE_FOR_SUBJECT_BUCKET, context.userId(),
              subjectBucket);
    } else {
      maxSequenceId = Base
          .firstCell(AJEntityCourse.SELECT_MAX_SEQUENCE_FOR_NON_SUBJECT_BUCKET, context.userId());
    }

    if (maxSequenceId != null) {
      sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;
    }
    return sequenceId;
  }

  private void autoPopulateFields() {
    course.setOwnerId(context.userId());
    course.setCreatorId(context.userId());
    course.setModifierId(context.userId());
    course.setPublishStatus(AJEntityCourse.PUBLISH_STATUS_TYPE_UNPUBLISHED);
    course.setInteger(AJEntityCourse.LICENSE, LicenseUtil.getDefaultLicenseCode());
    course.setTenant(context.tenant());
    String tenantRoot = context.tenantRoot();
    if (tenantRoot != null && !tenantRoot.isEmpty()) {
      course.setTenantRoot(tenantRoot);
    }
    String version = TenantSettingsProviderBuilder.buildTenantSettingsProvider(context.tenant())
        .getDefaultCourseVersion();
    if (version != null) {
      course.setVersion(version);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.course.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }
  
  private static class DefaultPayloadValidator implements PayloadValidator {
  }
  
}
