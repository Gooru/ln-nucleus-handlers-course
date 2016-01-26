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

import io.vertx.core.json.JsonObject;

public class DeleteCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCourseHandler.class);
  private final ProcessorContext context;

  public DeleteCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id for delete");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for delete"), ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to delete course");
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
        LOGGER.warn("course {} is already deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course your are trying to delete is already deleted"),
                ExecutionStatus.FAILED);
      }

      // Only owner can delete the course
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.warn("user is anonymous or not owner of course for delete. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.warn("course {} not found to delete, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      AJEntityCourse ajEntityCourse = new AJEntityCourse();
      ajEntityCourse.setId(context.courseId());
      ajEntityCourse.set(AJEntityCourse.IS_DELETED, true);
      ajEntityCourse.setString(AJEntityCourse.MODIFIER_ID, context.userId());

      if (ajEntityCourse.save()) {
        LOGGER.info("course {} marked as deleted successfully", context.courseId());
        // TODO: Delete everything underneath this course i.e. U/L/C/A
        // Get all units associated with course and mark as deleted
        LazyList<AJEntityUnit> unitsOfCourse = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_ASSOCIATED_WITH_COURSE, context.courseId(), false);
        if (!unitsOfCourse.isEmpty()) {
          LOGGER.info("{} units found to mark as deleted", unitsOfCourse.size());
          for (AJEntityUnit unitToDelete : unitsOfCourse) {
            unitToDelete.setBoolean(AJEntityUnit.IS_DELETED, true);
            unitToDelete.setString(AJEntityUnit.MODIFIER_ID, context.userId());
            if (unitToDelete.save()) {
              LOGGER.debug("unit {} marked deleted.", unitToDelete.get(AJEntityUnit.UNIT_ID));
            } else {
              LOGGER.debug("unable to mark unit {} to deleted.", unitToDelete.get(AJEntityUnit.UNIT_ID));
            }
          }
        } else {
          LOGGER.info("no units found to delete");
        }

        // Get all lessons associated with course and mark as deleted
        LazyList<AJEntityLesson> lessonsOfCourse =
                AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_ASSOCIATED_WITH_COURSE, context.courseId(), false);
        if (!lessonsOfCourse.isEmpty()) {
          LOGGER.info("{} lessons found to mark as deleted", lessonsOfCourse.size());
          for (AJEntityLesson lessonToDelete : lessonsOfCourse) {
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

        // Get all Collections/Assessments associated with course and mark as
        // deleted
        LazyList<AJEntityCollection> collectionsOfCourse =
                AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTIONS_ASSOCIATED_WITH_COURSE, context.courseId(), false);
        if (!collectionsOfCourse.isEmpty()) {
          LOGGER.info("{} collections/assessments found to mark as deleted", collectionsOfCourse.size());
          for (AJEntityCollection collectionToDelete : collectionsOfCourse) {
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

        // Get all Resources/Questions associated with course and mark as
        // deleted
        LazyList<AJEntityContent> contentsOfCourse =
                AJEntityContent.findBySQL(AJEntityContent.SELECT_CONTENT_ASSOCIATED_WITH_COURSE, context.courseId(), false);
        if (!contentsOfCourse.isEmpty()) {
          LOGGER.info("{} resources/question found to mark as deleted", contentsOfCourse.size());
          for (AJEntityContent contentToDelete : contentsOfCourse) {
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
        LOGGER.error("error in delete course");
        if (ajEntityCourse.hasErrors()) {
          Map<String, String> errMap = ajEntityCourse.errors();
          JsonObject errors = new JsonObject();
          errMap.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionStatus.FAILED);
        } else {
          return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Error in deleting course"), ExecutionStatus.FAILED);
        }
      }
    } catch (Throwable t) {
      LOGGER.error("exception while delete course", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
