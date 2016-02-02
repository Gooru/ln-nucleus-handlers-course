package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MoveUnitToCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoveUnitToCourseHandler.class);
  private final ProcessorContext context;
  private AJEntityUnit unitToUpdate;
  private String targetCourseOwner;

  public MoveUnitToCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to move unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id to move unit"), ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to move unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid data provided to move unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to move unit"), ExecutionStatus.FAILED);
    }

    JsonObject missingFieldErrors = validateMissingFields();
    if (missingFieldErrors != null && !missingFieldErrors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(missingFieldErrors), ExecutionResult.ExecutionStatus.FAILED);
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

    // 1. Check source course is not deleted
    // 2. Check target course is not deleted
    // 3. Check user is either owner or collaborator on source or target course
    // 4. Check whether unit is associated with source course or not
    String targetCourseId = context.courseId();
    String sourceCourseId = context.request().getString("course_id");
    String unitToMove = context.request().getString("unit_id");

    if (sourceCourseId == null || unitToMove == null || sourceCourseId.isEmpty() || unitToMove.isEmpty()) {
      LOGGER.debug("missing required fields");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(new JsonObject().put("message", "missing required fields")),
              ExecutionResult.ExecutionStatus.FAILED);
    }

    LazyList<AJEntityCourse> targetCourses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, targetCourseId, false);
    LazyList<AJEntityCourse> sourceCourses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, sourceCourseId, false);

    if (targetCourses.isEmpty() || sourceCourses.isEmpty()) {
      LOGGER.debug("source or target course is not found in database");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("source or target course is deleted"), ExecutionStatus.FAILED);
    }

    AJEntityCourse targetCourse = targetCourses.get(0);
    AJEntityCourse sourceCourse = sourceCourses.get(0);

    targetCourseOwner = targetCourse.getString(AJEntityCourse.OWNER_ID);
    if (!targetCourseOwner.equalsIgnoreCase(context.userId())
            || !new JsonArray(targetCourse.getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
      LOGGER.warn("user is not owner or collaborator of target course to move unit. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    if (!sourceCourse.getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())
            || !new JsonArray(sourceCourse.getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
      LOGGER.warn("user is not owner or collaborator of source course to move unit. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> units = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, unitToMove, targetCourseId, false);
    if (units.isEmpty()) {
      LOGGER.warn("Unit {} not found to move, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    unitToUpdate.setCourseId(context.courseId());
    unitToUpdate.setModifierId(context.userId());
    unitToUpdate.setOwnerId(targetCourseOwner);

    // Get max sequence id for course
    Object maxSequenceId = Base.firstCell(AJEntityUnit.SELECT_UNIT_MAX_SEQUENCEID, context.courseId());
    int sequenceId = 1;
    if (maxSequenceId != null) {
      sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;

    }
    unitToUpdate.set(AJEntityUnit.SEQUENCE_ID, sequenceId);

    if (unitToUpdate.hasErrors()) {
      LOGGER.debug("moving unit has errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }

    if (unitToUpdate.save()) {
      LOGGER.info("unit is moved to course");

      AJEntityLesson.update("course_id = ?::uuid, owner_id = ?::uuid, modifier_id = ?::uuid", "unit_id = ?::uuid", context.courseId(),
              targetCourseOwner, context.userId(), unitToUpdate.getId());
      AJEntityCollection.update("course_id = ?::uuid, owner_id = ?::uuid, modifier_id = ?::uuid, collaborator = ?", "unit_id = ?::uuid",
              context.courseId(), targetCourseOwner, context.userId(), null, unitToUpdate.getId());
      AJEntityContent.update("course_id = ?::uuid, modifier_id = ?::uuid", "unit_id = ?::uuid", context.courseId(), context.userId(),
              unitToUpdate.getId());
      return new ExecutionResult<>(MessageResponseFactory.createNoContentResponse(EventBuilderFactory.getMoveUnitEventBuilder(context.courseId())), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.debug("error while moving unit to course");
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
    input.fieldNames().stream().filter(key -> !AJEntityCourse.UNIT_MOVE_NOTNULL_FIELDS.contains(key))
            .forEach(key -> output.put(key, "Field not allowed"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject validateNullFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    input.fieldNames().stream()
            .filter(key -> AJEntityCourse.UNIT_MOVE_NOTNULL_FIELDS.contains(key)
                    && (input.getValue(key) == null || input.getValue(key).toString().isEmpty()))
            .forEach(key -> output.put(key, "Field should not be empty or null"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject validateMissingFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();

    AJEntityCourse.UNIT_MOVE_NOTNULL_FIELDS.stream().filter(field -> !input.containsKey(field))
            .forEach(field -> output.put(field, "Mandatory field"));
    return output;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.unitToUpdate.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

}
