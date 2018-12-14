package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReorderUnitInCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReorderUnitInCourseHandler.class);
  private final ProcessorContext context;
  private static final String REORDER_PAYLOAD_ID = "id";
  private static final String REORDER_PAYLOAD_KEY = "order";
  private static final String REORDER_PAYLOAD_SEQUENCE = "sequence_id";

  public ReorderUnitInCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to reorder units");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid course id provided to reorder units"),
          ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to reorder units");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid data provided to reorder units"),
          ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    if (!reorderPayloadValidator(context.request().getJsonArray(REORDER_PAYLOAD_KEY))) {
      LOGGER.warn("Request data validation failed");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(
          new JsonObject()
              .put("Reorder", "Data validation failed. Invalid data in request payload")),
          ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse
        .findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, context.courseId(), false,
            context.userId(),
            context.userId());
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of course to reorder units. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {

      ExecutionResult<MessageResponse> errors = DbHelperUtil.updateCourseTimestamp(context, LOGGER);
      if (errors != null) {
        return errors;
      }
      List unitsOfCourse = Base
          .firstColumn(AJEntityUnit.SELECT_UNIT_OF_COURSE, context.courseId(), false);
      JsonArray input = this.context.request().getJsonArray(REORDER_PAYLOAD_KEY);

      if (unitsOfCourse.size() != input.size()) {
        return new ExecutionResult<>(
            MessageResponseFactory.createInvalidRequestResponse("Unit count mismatch"),
            ExecutionResult.ExecutionStatus.FAILED);
      }

      PreparedStatement ps = Base.startBatch(AJEntityUnit.REORDER_QUERY);

      for (Object entry : input) {
        String payloadUnitId = ((JsonObject) entry).getString(REORDER_PAYLOAD_ID);
        if (!unitsOfCourse.contains(UUID.fromString(payloadUnitId))) {
          return new ExecutionResult<>(
              MessageResponseFactory.createInvalidRequestResponse("Missing unit(s)"),
              ExecutionResult.ExecutionStatus.FAILED);
        }

        int sequenceId = ((JsonObject) entry).getInteger(AJEntityUnit.SEQUENCE_ID);
        Base.addBatch(ps, sequenceId, this.context.userId(), payloadUnitId, context.courseId(),
            false);
      }

      Base.executeBatch(ps);
    } catch (DBException | ClassCastException e) {
      LOGGER.error("incorrect payload data type", e);
      return new ExecutionResult<>(
          MessageResponseFactory.createInvalidRequestResponse("Incorrect payload data types"),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    LOGGER.info("reordered units in course {}", context.courseId());
    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(
            EventBuilderFactory.getReorderUnitEventBuilder(context.courseId())),
        ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private boolean reorderPayloadValidator(Object value) {
    if (!(value instanceof JsonArray) || value == null || ((JsonArray) value).isEmpty()) {
      return false;
    }
    JsonArray input = (JsonArray) value;
    List<Integer> sequences = new ArrayList<>(input.size());
    for (Object o : input) {
      if (!(o instanceof JsonObject)) {
        return false;
      }
      JsonObject entry = (JsonObject) o;
      if ((entry.getMap().keySet().isEmpty() || entry.getMap().keySet().size() != 2)) {
        return false;
      }
      try {
        Integer sequence = entry.getInteger(REORDER_PAYLOAD_SEQUENCE);
        if (sequence == null) {
          return false;
        }
        String idString = entry.getString(REORDER_PAYLOAD_ID);
        UUID id = UUID.fromString(idString);
        sequences.add(sequence);
      } catch (ClassCastException | IllegalArgumentException e) {
        return false;
      }
    }
    if (sequences.size() != input.size()) {
      return false;
    }
    for (int i = 1; i <= input.size(); i++) {
      if (!sequences.contains(i)) {
        return false;
      }
    }
    return true;
  }

}
