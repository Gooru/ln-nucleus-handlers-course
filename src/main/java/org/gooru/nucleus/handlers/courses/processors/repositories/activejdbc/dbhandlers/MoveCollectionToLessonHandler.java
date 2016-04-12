package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.Timestamp;
import java.util.Map;

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

import io.vertx.core.json.JsonObject;

public class MoveCollectionToLessonHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCourseHandler.class);
  private final ProcessorContext context;
  private AJEntityCollection collectionToUpdate;
  private String targetCourseOwner;

  public MoveCollectionToLessonHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to move collection/assessment");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id to move collection/assessment"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to move collection/assessment");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id to move collection/assessment"),
              ExecutionStatus.FAILED);
    }

    if (context.lessonId() == null || context.lessonId().isEmpty()) {
      LOGGER.warn("invalid lesson id to move collection/assessment");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id to move collection/assessment"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to move collection/assessment");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("invalid data provided to move collection/assessment");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to move collection/assessment"),
              ExecutionStatus.FAILED);
    }

    JsonObject mandatoryFieldErrors = validateMandatoryFields();
    if (mandatoryFieldErrors != null && !mandatoryFieldErrors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(mandatoryFieldErrors),
              ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // first check whether collection is part of CUL, if its part of CUL then
    // validate for both source and target
    // If collection is not part of CUL then validate only target

    String targetCourseId = context.courseId();
    String targetUnitId = context.unitId();
    String targetLessonId = context.lessonId();
    String collectionToMove = context.request().getString("collection_id");
    // String type = context.request().getString("type");

    LazyList<AJEntityCourse> targetCourses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, targetCourseId, false,
      context.userId(), context.userId());
    if (targetCourses.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of target course to move collection. aborting");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    targetCourseOwner = targetCourses.get(0).getString(AJEntityCourse.OWNER_ID);
    
    LazyList<AJEntityUnit> targetUnits = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, targetUnitId, targetCourseId, false);
    if (targetUnits.isEmpty()) {
      LOGGER.warn("target unit is not found in database");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("target unit is not found"), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityLesson> targetLessons =
            AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, targetLessonId, targetUnitId, targetCourseId, false);
    if (targetLessons.isEmpty()) {
      LOGGER.warn("target lesson is not found in database");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("target lesson is not found"), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityCollection> collections = AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTION_TO_MOVE, collectionToMove, false);
    if (collections.isEmpty()) {
      LOGGER.warn("collection not exist in database to move");
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Collection not found"), ExecutionStatus.FAILED);
    }

    collectionToUpdate = collections.get(0);
    // If request contains course_id, then collection to move is associated with
    // CUL
    if (context.request().containsKey(AJEntityCollection.COURSE_ID)) {

      JsonObject notNullErrors = validateNullFields();
      if (notNullErrors != null && !notNullErrors.isEmpty()) {
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(notNullErrors), ExecutionResult.ExecutionStatus.FAILED);
      }

      String sourceCourseId = context.request().getString("course_id");
      String sourceUnitId = context.request().getString("unit_id");
      String sourceLessonId = context.request().getString("lesson_id");

      // Check whether source course id is also exists in db
      if (!sourceCourseId.equalsIgnoreCase(collectionToUpdate.getString(AJEntityCollection.COURSE_ID))) {
        LOGGER.warn("collection is not associated with source course");
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Collection is not associated with source course"),
                ExecutionStatus.FAILED);
      }

      if (!sourceUnitId.equalsIgnoreCase(collectionToUpdate.getString(AJEntityCollection.UNIT_ID))) {
        LOGGER.warn("collection is not associated with source unit");
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Collection is not associated with source unit"),
                ExecutionStatus.FAILED);
      }

      if (!sourceLessonId.equalsIgnoreCase(collectionToUpdate.getString(AJEntityCollection.LESSON_ID))) {
        LOGGER.warn("collection is not associated with source lesson");
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Collection is not associated with source lesson"),
                ExecutionStatus.FAILED);
      }

      LazyList<AJEntityCourse> sourceCourses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, sourceCourseId, false,
        context.userId(), context.userId());
      if (sourceCourses.isEmpty()) {
        LOGGER.warn("user is not owner or collaborator of source course to move collection. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }

      LazyList<AJEntityUnit> sourceUnits = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, sourceUnitId, sourceCourseId, false);
      if (sourceUnits.isEmpty()) {
        LOGGER.warn("source unit is not found in database");
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("source unit is not found"), ExecutionStatus.FAILED);
      }

      LazyList<AJEntityLesson> sourceLessons =
              AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, sourceLessonId, sourceUnitId, sourceCourseId, false);
      if (sourceLessons.isEmpty()) {
        LOGGER.warn("source lesson is not found in database");
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("source lesson is not found"), ExecutionStatus.FAILED);
      }
    } else {
      // if the course_id is null in request, then its not associated with CUL
      // As the collection is not part of CUL, check whether the user is owner
      // of collection to move it
      if (!collectionToUpdate.getString("owner_id").equalsIgnoreCase(context.userId())) {
        LOGGER.warn("user is not owner of collection to move");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("user is not owner of course"), ExecutionStatus.FAILED);
      }
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    collectionToUpdate.setCourseId(context.courseId());
    collectionToUpdate.setUnitId(context.unitId());
    collectionToUpdate.setLessonId(context.lessonId());
    collectionToUpdate.setModifierId(context.userId());
    collectionToUpdate.setOwnerId(targetCourseOwner);
    collectionToUpdate.set(AJEntityCollection.COLLABORATOR, null);

    Object maxSequenceId = Base.firstCell(AJEntityCollection.SELECT_COLLECTION_MAX_SEQUENCEID, context.lessonId());
    int sequenceId = 1;
    if (maxSequenceId != null) {
      sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;
    }
    collectionToUpdate.set(AJEntityCollection.SEQUENCE_ID, sequenceId);

    if (collectionToUpdate.hasErrors()) {
      LOGGER.warn("moving collection has errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }

    if (collectionToUpdate.save()) {
      LOGGER.info("collection is moved to course");
      AJEntityContent.update("course_id = ?::uuid, unit_id = ?::uuid, lesson_id = ?::uuid, modifier_id = ?::uuid", "collection_id = ?::uuid",
              context.courseId(), context.unitId(), context.lessonId(), context.userId(), collectionToUpdate.getId());
      
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
      
      return new ExecutionResult<>(
              MessageResponseFactory.createNoContentResponse(EventBuilderFactory.getMoveCollectionEventBuilder(context.lessonId())),
              ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("error while moving collection to lesson");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject validateNullFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    input.fieldNames().stream()
            .filter(key -> AJEntityCollection.COLLECTION_MOVE_NOTNULL_FIELDS.contains(key)
                    && (input.getValue(key) == null || input.getValue(key).toString().isEmpty()))
            .forEach(key -> output.put(key, "Field should not be empty or null"));
    return output.isEmpty() ? null : output;
  }

  private JsonObject validateMandatoryFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();

    if (!input.containsKey("collection_id")) {
      output.put("collection_id", "Mandatory field");
      return output;
    }

    if (input.getString("collection_id") == null || input.getString("collection_id").isEmpty()) {
      output.put("collection_id", "Field should not be empty or null");
    }

    return output;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.collectionToUpdate.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

}
