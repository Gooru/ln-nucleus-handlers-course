package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.Timestamp;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class RemoveCollectionFromLessonHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCollectionFromLessonHandler.class);
    private final ProcessorContext context;

    public RemoveCollectionFromLessonHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to remove collection");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityCourse> courses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE,
            context.courseId(), false, context.userId(), context.userId());
        if (courses.isEmpty()) {
            LOGGER.warn("user is not owner or collaborator of course to remove collection. aborting");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        LazyList<AJEntityCollection> collections =
            AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTION_TO_VALIDATE, context.collectionId(),
                context.courseId(), context.unitId(), context.lessonId());
        if (collections.isEmpty()) {
            LOGGER.warn("{} collection not found to remove", context.collectionId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("validateRequest() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        int updateCount = AJEntityCollection.update("course_id = null, unit_id = null, lesson_id = null",
            "id = ?::uuid", context.collectionId());

        if (updateCount == 0) {
            LOGGER.warn("Unable to remove collection '{}' from lesson", context.collectionId());
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("unable to remove collection from lesson"),
                ExecutionStatus.FAILED);
        }

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
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                    ExecutionStatus.FAILED);
            }
        }
        
        return new ExecutionResult<>(MessageResponseFactory
            .createNoContentResponse(EventBuilderFactory.getRemoveCollectionFromLessonEventBuilder(context.courseId(),
                context.unitId(), context.lessonId(), context.collectionId())),
            ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
