package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.Timestamp;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class CreateUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateUnitHandler.class);
  private AJEntityUnit newUnit;
  private String courseOwner;

  public CreateUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {

    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to delete unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to delete unit"),
        ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to create unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create unit"),
        ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to create unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    JsonObject validateErrors = validateFields();
    if (validateErrors != null && !validateErrors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(validateErrors), ExecutionResult.ExecutionStatus.FAILED);
    }

    JsonObject notNullErrors = validateNullFields();
    if (notNullErrors != null && !notNullErrors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(notNullErrors), ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, context.courseId(), false,
      context.userId(), context.userId());
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of course to create unit. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }
    courseOwner = ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID);

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    newUnit = new AJEntityUnit();
    newUnit.setAllFromJson(context.request());
    newUnit.setCourseId(context.courseId());
    newUnit.setOwnerId(courseOwner);
    newUnit.setCreatorId(context.userId());
    newUnit.setModifierId(context.userId());
    newUnit.set(AJEntityUnit.IS_DELETED, false);

    // Get max sequence id for course
    Object maxSequenceId = Base.firstCell(AJEntityUnit.SELECT_UNIT_MAX_SEQUENCEID, context.courseId());
    int sequenceId = 1;
    if (maxSequenceId != null) {
      sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;

    }
    newUnit.set(AJEntityUnit.SEQUENCE_ID, sequenceId);

    if (newUnit.hasErrors()) {
      LOGGER.warn("error in creating new unit");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }

    if (newUnit.isValid()) {
      if (newUnit.save()) {
        LOGGER.info("unit {} created successfully for course {}", newUnit.getId().toString(), context.courseId());
        
        AJEntityCourse courseToUpdate = new AJEntityCourse();
        courseToUpdate.setCourseId(context.courseId());
        courseToUpdate.setTimestamp(AJEntityCourse.UPDATED_AT, new Timestamp(System.currentTimeMillis()));
        boolean result = courseToUpdate.save(); 
        if (!result) {
          LOGGER.error("Course with id '{}' failed to save modified time stamp", context.courseId());
          if (courseToUpdate.hasErrors()) {
            Map<String, String> map = courseToUpdate.errors();
            JsonObject errors = new JsonObject();
            map.forEach(errors::put);
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
          }
        }
        
        return new ExecutionResult<>(MessageResponseFactory
          .createPostResponse(newUnit.getId().toString(), EventBuilderFactory.getCreateUnitEventBuilder(newUnit.getId().toString())),
          ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("error in saving unit");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("validation error in saving unit");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject validateFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    input.fieldNames().stream().filter(key -> !AJEntityUnit.INSERTABLE_FIELDS.contains(key)).forEach(key -> output.put(key, "Field not allowed"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject validateNullFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    AJEntityUnit.NOTNULL_FIELDS.stream()
                                 .filter(notNullField -> (input.getValue(notNullField) == null || input.getValue(notNullField).toString().isEmpty()))
                                 .forEach(notNullField -> output.put(notNullField, "Field should not be empty or null"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.newUnit.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

}
