package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DeleteUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteUnitHandler.class);

  public DeleteUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to delete unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to delete unit"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to delete unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to delete unit"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to delete unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is already deleted, hence can't delete unit. Aborting", context.courseId());
        return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse("Course is already deleted for which you are trying to delete unit"),
                ExecutionStatus.FAILED);
      }

      // check whether user is either owner or collaborator
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        if (!new JsonArray(ajEntityCourse.get(0).getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
          LOGGER.warn("user is not owner or collaborator of course to create unit. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.warn("course {} not found to delete unit, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId());
    if (!ajEntityUnit.isEmpty()) {
      if (ajEntityUnit.get(0).getBoolean(AJEntityUnit.IS_DELETED)) {
        LOGGER.info("unit {} is already deleted. Aborting", context.unitId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Unit is already deleted"), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.info("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    AJEntityUnit unitToDelete = new AJEntityUnit();
    unitToDelete.setId(context.unitId());
    unitToDelete.setBoolean(AJEntityUnit.IS_DELETED, true);
    unitToDelete.setString(AJEntityUnit.MODIFIER_ID, context.userId());

    if (unitToDelete.save()) {
      LOGGER.info("unit {} marked as deleted successfully", context.unitId());
      // Get all lessons associated with unit and mark as deleted
      LazyList<AJEntityLesson> lessonsOfUnit = AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_ASSOCIATED_WITH_UNIT, context.unitId(), false);
      if (!lessonsOfUnit.isEmpty()) {
        LOGGER.info("{} lessons found to mark as deleted", lessonsOfUnit.size());
        for (AJEntityLesson lessonToDelete : lessonsOfUnit) {
          lessonToDelete.setBoolean(AJEntityLesson.IS_DELETED, true);
          lessonToDelete.setString(AJEntityLesson.MODIFIER_ID, context.userId());
          if (lessonToDelete.save()) {
            LOGGER.debug("lesson {} marked deleted.", lessonToDelete.get(AJEntityLesson.LESSON_ID));
          } else {
            LOGGER.debug("unable to mark lesson {} to deleted.", lessonToDelete.get(AJEntityLesson.LESSON_ID));
          }
        }
      } else {
        LOGGER.info("no lessons found to delete");
      }

      // Get all Collections/Assessments associated with unit and mark as
      // deleted
      LazyList<AJEntityCollection> collectionsOfUnit =
              AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTIONS_ASSOCIATED_WITH_UNIT, context.unitId(), false);
      if (!collectionsOfUnit.isEmpty()) {
        LOGGER.info("{} collections/assessments found to mark as deleted", collectionsOfUnit.size());
        for (AJEntityCollection collectionToDelete : collectionsOfUnit) {
          collectionToDelete.setBoolean(AJEntityCollection.IS_DELETED, true);
          collectionToDelete.setString(AJEntityCollection.MODIFIER_ID, context.userId());
          if (collectionToDelete.save()) {
            LOGGER.debug("collection/assessment {} marked as deleted", collectionToDelete.get(AJEntityCollection.ID));
          } else {
            LOGGER.debug("unable to mark collection/assessment {} to deleted.", collectionToDelete.get(AJEntityCollection.ID));
          }
        }
      } else {
        LOGGER.info("no collection/assessment found to delete");
      }

      // Get all Resources/Questions associated with unit and mark as deleted
      LazyList<AJEntityContent> contentsOfUnit =
              AJEntityContent.findBySQL(AJEntityContent.SELECT_CONTENT_ASSOCIATED_WITH_UNIT, context.unitId(), false);
      if (!contentsOfUnit.isEmpty()) {
        LOGGER.info("{} resources/question found to mark as deleted", contentsOfUnit.size());
        for (AJEntityContent contentToDelete : contentsOfUnit) {
          contentToDelete.setBoolean(AJEntityContent.IS_DELETED, true);
          // contentToDelete.setString(AJEntityContent.MODIFIER_ID,
          // context.userId());
          if (contentToDelete.save()) {
            LOGGER.debug("resource/question {} marked as deleted", contentToDelete.get(AJEntityContent.ID));
          } else {
            LOGGER.debug("unable to mark resource/question {} to deleted", contentToDelete.get(AJEntityContent.ID));
          }
        }
      } else {
        LOGGER.info("no resources/questions found to delete");
      }

      return new ExecutionResult<>(MessageResponseFactory.createDeleteResponse(), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("error in delete unit");
      if (unitToDelete.hasErrors()) {
        Map<String, String> errMap = unitToDelete.errors();
        JsonObject errors = new JsonObject();
        errMap.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
      } else {
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error in deleting unit"), ExecutionStatus.FAILED);
      }
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
