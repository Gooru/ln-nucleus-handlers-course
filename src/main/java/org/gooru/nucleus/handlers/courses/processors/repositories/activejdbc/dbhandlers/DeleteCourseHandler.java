package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityClass;
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
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class DeleteCourseHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCourseHandler.class);
    private final ProcessorContext context;
    private AJEntityCourse courseToDelete;

    public DeleteCourseHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id for delete");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid course id for delete"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to delete course");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {

        LazyList<AJEntityCourse> ajEntityCourse =
            AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
        if (!ajEntityCourse.isEmpty()) {
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

        courseToDelete = new AJEntityCourse();
        courseToDelete.setCourseId(context.courseId());
        courseToDelete.setModifierId(context.userId());
        courseToDelete.setBoolean(AJEntityCourse.IS_DELETED, true);

        if (courseToDelete.hasErrors()) {
            LOGGER.warn("deleting course has errors");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }

        if (courseToDelete.save()) {
            LOGGER.info("course {} marked as deleted successfully", context.courseId());

            // Update rest of the hierarchy of the course to deleted
            AJEntityUnit.update("is_deleted = ?, modifier_id = ?::uuid", "course_id = ?::uuid", true, context.userId(),
                context.courseId());
            AJEntityLesson.update("is_deleted = ?, modifier_id = ?::uuid", "course_id = ?::uuid", true,
                context.userId(), context.courseId());
            AJEntityCollection.update("is_deleted = ?, modifier_id = ?::uuid", "course_id = ?::uuid", true,
                context.userId(), context.courseId());
            AJEntityContent.update("is_deleted = ?, modifier_id = ?::uuid", "course_id = ?::uuid", true,
                context.userId(), context.courseId());
            AJEntityRubric.update("is_deleted = ?, modifier_id = ?::uuid", "course_id = ?::uuid", true,
                context.userId(), context.courseId());

            // Remove the association of this course from class
            // class is not archived and not delete and version is current
            // version
			// Update- 17/Jul/2018 - NILE-2835: Make the setting as null while removing the
			// class course association. For now we are making it null, however we should
			// remove the key from Jsonb instead of to make it null.
            AJEntityClass.update("course_id = null, setting = null",
                "course_id = ?::uuid AND is_deleted = ? AND is_archived = ? AND gooru_version = ?", context.courseId(),
                false, false, AJEntityClass.CURRENT_VERSION);

            return new ExecutionResult<>(
                MessageResponseFactory.createNoContentResponse(
                    EventBuilderFactory.getDeleteCourseEventBuilder(courseToDelete.getId().toString())),
                ExecutionStatus.SUCCESSFUL);
        } else {
            LOGGER.error("error while deleting course");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        this.courseToDelete.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }
}
