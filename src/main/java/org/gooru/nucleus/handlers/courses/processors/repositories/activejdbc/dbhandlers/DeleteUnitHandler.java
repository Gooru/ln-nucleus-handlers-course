package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.tagaggregator.TagAggregatorRequestBuilderFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteUnitHandler.class);
  private AJEntityUnit unitToDelete;
  private AJEntityUnit existingUnit;

  public DeleteUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to delete unit");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid course id provided to delete unit"),
          ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to delete unit");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid unit id provided to delete unit"),
          ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to delete unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse =
        AJEntityCourse
            .findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
    if (!ajEntityCourse.isEmpty()) {
      // check whether user is either owner or collaborator
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID)
          .equalsIgnoreCase(context.userId())) {
        String collaborators = ajEntityCourse.get(0).getString(AJEntityCourse.COLLABORATOR);
        if (collaborators == null || !new JsonArray(collaborators).contains(context.userId())) {
          LOGGER.warn("user is not owner or collaborator of course to create unit. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
              ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.warn("course {} not found to delete unit, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit =
        AJEntityUnit
            .findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(),
                false);
    if (ajEntityUnit.isEmpty()) {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    this.existingUnit = ajEntityUnit.get(0);

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    ExecutionResult<MessageResponse> errors = DbHelperUtil.updateCourseTimestamp(context, LOGGER);
    if (errors != null) {
      return errors;
    }

    unitToDelete = new AJEntityUnit();
    unitToDelete.setUnitId(context.unitId());
    unitToDelete.setBoolean(AJEntityUnit.IS_DELETED, true);
    unitToDelete.setModifierId(context.userId());

    if (unitToDelete.hasErrors()) {
      LOGGER.warn("deleting unit has errors");
      return new ExecutionResult<>(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
          ExecutionStatus.FAILED);
    }

    if (unitToDelete.save()) {
      LOGGER.info("unit {} marked as deleted successfully", context.unitId());

      AJEntityLesson.update("is_deleted = ?, modifier_id = ?::uuid",
          "course_id = ?::uuid and unit_id = ?::uuid",
          true, context.userId(), context.courseId(), context.unitId());
      AJEntityCollection.update("is_deleted = ?, modifier_id = ?::uuid",
          "course_id = ?::uuid and unit_id = ?::uuid", true, context.userId(), context.courseId(),
          context.unitId());
      AJEntityContent.update("is_deleted = ?, modifier_id = ?::uuid",
          "course_id = ?::uuid and unit_id = ?::uuid",
          true, context.userId(), context.courseId(), context.unitId());
      AJEntityRubric.update("is_deleted = ?, modifier_id = ?::uuid",
          "course_id = ?::uuid and unit_id = ?::uuid",
          true, context.userId(), context.courseId(), context.unitId());

      // Calculate tag difference. If tags are present for the entity then
      // send the request for aggregation at Course Level, otherwise skip
      JsonObject tagDiff = DbHelperUtil.generateTagsToDelete(this.existingUnit);
      if (tagDiff != null && !tagDiff.isEmpty()) {
        JsonArray tagsToAggregate = new JsonArray();
        tagsToAggregate.add(TagAggregatorRequestBuilderFactory
            .getCourseTagAggregatorRequestBuilder(this.context.courseId(), tagDiff).build());
        return new ExecutionResult<>(MessageResponseFactory.createNoContentResponse(
            EventBuilderFactory.getDeleteUnitEventBuilder(unitToDelete.getId().toString()),
            tagsToAggregate),
            ExecutionStatus.SUCCESSFUL);
      }

      return new ExecutionResult<>(MessageResponseFactory.createNoContentResponse(
          EventBuilderFactory.getDeleteUnitEventBuilder(unitToDelete.getId().toString())),
          ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("error while deleting unit");
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
    this.unitToDelete.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }
}
