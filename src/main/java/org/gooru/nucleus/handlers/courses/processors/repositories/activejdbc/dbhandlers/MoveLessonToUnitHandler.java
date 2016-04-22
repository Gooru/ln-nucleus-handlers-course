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

public class MoveLessonToUnitHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveLessonToUnitHandler.class);
    private final ProcessorContext context;
    private AJEntityLesson lessonToUpdate;
    private String targetCourseOwner;

    public MoveLessonToUnitHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id to move lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid course id to move lesson"),
                ExecutionStatus.FAILED);
        }

        if (context.unitId() == null || context.unitId().isEmpty()) {
            LOGGER.warn("invalid unit id to move lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid unit id to move lesson"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to move lesson");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("invalid data provided to move lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to move lesson"),
                ExecutionStatus.FAILED);
        }

        JsonObject missingFieldErrors = validateMissinFields();
        if (missingFieldErrors != null && !missingFieldErrors.isEmpty()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(missingFieldErrors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        JsonObject validateErrors = validateFields();
        if (validateErrors != null && !validateErrors.isEmpty()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(validateErrors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        JsonObject notNullErrors = validateNullFields();
        if (notNullErrors != null && !notNullErrors.isEmpty()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(notNullErrors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        String targetCourseId = context.courseId();
        String targetUnitId = context.unitId();
        String sourceCourseId = context.request().getString("course_id");
        String sourceUnitId = context.request().getString("unit_id");
        String lessonToMove = context.request().getString("lesson_id");

        LazyList<AJEntityCourse> targetCourses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE,
            targetCourseId, false, context.userId(), context.userId());
        LazyList<AJEntityCourse> sourceCourses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE,
            sourceCourseId, false, context.userId(), context.userId());

        if (targetCourses.isEmpty() || sourceCourses.isEmpty()) {
            LOGGER.warn("user is not owner or collaborator of source or target course to move lesson. aborting");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        AJEntityCourse targetCourse = targetCourses.get(0);
        targetCourseOwner = targetCourse.getString(AJEntityCourse.OWNER_ID);

        // Check whether the source unit is exists, not deleted and associated
        // with
        // source course
        LazyList<AJEntityUnit> sourceUnits =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, sourceUnitId, sourceCourseId, false);
        if (sourceUnits.isEmpty()) {
            LOGGER.warn("Unit {} not found to move, aborting", context.unitId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        // Check whether the target unit is exists, not deleted and associated
        // with
        // target course
        LazyList<AJEntityUnit> targetUnits =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, targetUnitId, targetCourseId, false);
        if (targetUnits.isEmpty()) {
            LOGGER.warn("Unit {} not found to move, aborting", context.unitId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        // Check whether the lesson is exists, not deleted and associated with
        // source unit and course
        LazyList<AJEntityLesson> lessons = AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE,
            lessonToMove, sourceUnitId, sourceCourseId, false);
        if (lessons.isEmpty()) {
            LOGGER.warn("Lesson {} not found to move, aborting", context.unitId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        lessonToUpdate = lessons.get(0);

        LOGGER.debug("validateRequest() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        lessonToUpdate.setCourseId(context.courseId());
        lessonToUpdate.setUnitId(context.unitId());
        lessonToUpdate.setModifierId(context.userId());
        lessonToUpdate.setOwnerId(targetCourseOwner);

        Object maxSequenceId =
            Base.firstCell(AJEntityLesson.SELECT_LESSON_MAX_SEQUENCEID, context.courseId(), context.unitId());
        int sequenceId = 1;
        if (maxSequenceId != null) {
            sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;
        }
        lessonToUpdate.set(AJEntityLesson.SEQUENCE_ID, sequenceId);

        if (lessonToUpdate.hasErrors()) {
            LOGGER.warn("moving lesson has errors");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }

        if (lessonToUpdate.save()) {
            LOGGER.info("lesson is moved to unit and course");
            AJEntityCollection.update(
                "course_id = ?::uuid, unit_id = ?::uuid, owner_id = ?::uuid, modifier_id = ?::uuid, collaborator = ?",
                "lesson_id = ?::uuid", context.courseId(), context.unitId(), targetCourseOwner, context.userId(), null,
                lessonToUpdate.getId());
            AJEntityContent.update("course_id = ?::uuid, unit_id = ?::uuid, modifier_id = ?::uuid",
                "lesson_id = ?::uuid", context.courseId(), context.unitId(), context.userId(), lessonToUpdate.getId());

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

            return new ExecutionResult<>(
                MessageResponseFactory.createNoContentResponse(EventBuilderFactory
                    .getMoveLessonEventBuilder(context.courseId(), context.unitId(), context.request())),
                ExecutionStatus.SUCCESSFUL);
        } else {
            LOGGER.error("error while moving lesson to course and unit");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private JsonObject validateFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();
        input.fieldNames().stream().filter(key -> !AJEntityUnit.LESSON_MOVE_NOTNULL_FIELDS.contains(key))
            .forEach(key -> output.put(key, "Field not allowed"));
        return output.isEmpty() ? null : output;
    }

    private JsonObject validateNullFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();
        input.fieldNames().stream()
            .filter(key -> AJEntityUnit.LESSON_MOVE_NOTNULL_FIELDS.contains(key)
                && (input.getValue(key) == null || input.getValue(key).toString().isEmpty()))
            .forEach(key -> output.put(key, "Field should not be empty or null"));
        return output.isEmpty() ? null : output;
    }

    private JsonObject validateMissinFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();

        AJEntityUnit.LESSON_MOVE_NOTNULL_FIELDS.stream().filter(field -> !input.containsKey(field))
            .forEach(field -> output.put(field, "Mandatory field"));
        return output;
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        this.lessonToUpdate.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }

}
