package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ReorderUnitInCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReorderUnitInCourseHandler.class);
  private final ProcessorContext context;
  private JsonArray input;

  public ReorderUnitInCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to reorder units"),
              ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to reorder units"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to reorder units");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
    if (!ajEntityCourse.isEmpty()) {
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        if (!new JsonArray(ajEntityCourse.get(0).getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
          LOGGER.warn("user is not owner or collaborator of course to reoder units. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.warn("course {} not found to reorder units, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      List unitsOfCourse = Base.firstColumn(AJEntityUnit.SELECT_UNIT_OF_COURSE, context.courseId(), false);
      this.input = this.context.request().getJsonArray(AJEntityUnit.REORDER_PAYLOAD_KEY);

      if (unitsOfCourse.size() != input.size()) {
        return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Unit count mismatch"),
                ExecutionResult.ExecutionStatus.FAILED);
      }

      PreparedStatement ps = Base.startBatch(AJEntityUnit.REORDER_QUERY);

      for (Object entry : input) {
        String payloadUnitId = ((JsonObject) entry).getString(AJEntityUnit.ID);
        if (!unitsOfCourse.contains(UUID.fromString(payloadUnitId))) {
          return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing unit(s)"),
                  ExecutionResult.ExecutionStatus.FAILED);
        }

        int sequenceId = ((JsonObject) entry).getInteger(AJEntityUnit.SEQUENCE_ID);
        Base.addBatch(ps, sequenceId, this.context.userId(), payloadUnitId, context.courseId(), false);
      }

      Base.executeBatch(ps);
    } catch (DBException | ClassCastException e) {
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Incorrect payload data types"),
              ExecutionResult.ExecutionStatus.FAILED);
    }
    LOGGER.info("reordered units in course {}", context.courseId());
    return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
