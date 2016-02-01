package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
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

public class ReorderCollectionsAssessmentsInLessonHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(ReorderCollectionsAssessmentsInLessonHandler.class);
  private JsonArray input;

  public ReorderCollectionsAssessmentsInLessonHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to reorder lesson contents");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to reorder lesson contents"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.info("invalid unit id to reorder lesson contents");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to reorder lesson contents"),
              ExecutionStatus.FAILED);
    }

    if (context.lessonId() == null || context.lessonId().isEmpty()) {
      LOGGER.info("invalid lesson id to reorder lesson contents");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to reorder lesson contents"),
              ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid request received to reorder lesson contents");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to reorder lesson contents"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to reorder lesson contents");
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
          LOGGER.warn("user is not owner or collaborator of course to reoder lesson content. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.warn("course {} not found to reorder lesson content, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(), false);
    if (ajEntityUnit.isEmpty()) {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityLesson> ajEntityLesson =
            AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, context.lessonId(), context.unitId(), context.courseId(), false);
    if (ajEntityLesson.isEmpty()) {
      LOGGER.warn("Lesson {} not found, aborting", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      List contentOfLesson =
              Base.firstColumn(AJEntityCollection.SELECT_COLLECTION_OF_COURSE, context.lessonId(), context.unitId(), context.courseId(), false);
      this.input = this.context.request().getJsonArray(AJEntityCollection.REORDER_PAYLOAD_KEY);

      if (contentOfLesson.size() != input.size()) {
        return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Collection/Assessment count mismatch"),
                ExecutionResult.ExecutionStatus.FAILED);
      }

      PreparedStatement ps = Base.startBatch(AJEntityCollection.REORDER_QUERY);

      for (Object entry : input) {
        String payloadContentId = ((JsonObject) entry).getString(AJEntityCollection.ID);
        if (!contentOfLesson.contains(UUID.fromString(payloadContentId))) {
          return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing collection(s)/assessment(s)"),
                  ExecutionResult.ExecutionStatus.FAILED);
        }

        int sequenceId = ((JsonObject) entry).getInteger(AJEntityCollection.SEQUENCE_ID);
        Base.addBatch(ps, sequenceId, this.context.userId(), payloadContentId, context.lessonId(), context.unitId(), context.courseId(), false);
      }

      Base.executeBatch(ps);
    } catch (DBException | ClassCastException e) {
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Incorrect payload data types"),
              ExecutionResult.ExecutionStatus.FAILED);
    }
    LOGGER.info("reordered contents of in lesson {}", context.lessonId());
    return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.lessonId()), ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
