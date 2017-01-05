package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.Timestamp;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
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

public class CreateLessonHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLessonHandler.class);
    private AJEntityLesson lesson;
    private String courseOwner;

    public CreateLessonHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id to create lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to create lesson"),
                ExecutionStatus.FAILED);
        }

        if (context.unitId() == null || context.unitId().isEmpty()) {
            LOGGER.warn("invalid unit id to create lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to create lesson"),
                ExecutionStatus.FAILED);
        }

        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("invalid request received to create lesson");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to create lesson"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to create lesson");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
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
        LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE,
            context.courseId(), false, context.userId(), context.userId());
        if (ajEntityCourse.isEmpty()) {
            LOGGER.warn("user is not owner or collaborator of course to create lesson. aborting");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        courseOwner = ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID);

        LazyList<AJEntityUnit> ajEntityUnit =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(), false);
        if (ajEntityUnit.isEmpty()) {
            LOGGER.warn("Unit {} not found, aborting", context.unitId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("validateRequest() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        lesson = new AJEntityLesson();
        lesson.setAllFromJson(context.request());
        autoPopulateFields();

        lesson.set(AJEntityLesson.SEQUENCE_ID, getSequenceId());

        if (lesson.hasErrors()) {
            LOGGER.warn("error in creating new lesson");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }

        if (lesson.isValid()) {
            if (lesson.save()) {
                LOGGER.info("lesson {} created successfully for unit {}", lesson.getId().toString(),
                    context.unitId());

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
                    MessageResponseFactory.createPostResponse(lesson.getId().toString(),
                        EventBuilderFactory.getCreateLessonEventBuilder(lesson.getId().toString())),
                    ExecutionStatus.SUCCESSFUL);
            } else {
                LOGGER.error("error while saving new lesson");
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                    ExecutionStatus.FAILED);
            }
        } else {
            LOGGER.warn("validation errors in creating new lesson");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }
    }

    private int getSequenceId() {
        Object maxSequenceId =
            Base.firstCell(AJEntityLesson.SELECT_LESSON_MAX_SEQUENCEID, context.courseId(), context.unitId());
        int sequenceId = 1;
        if (maxSequenceId != null) {
            sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;
        }
        return sequenceId;
    }

    private void autoPopulateFields() {
        lesson.setCourseId(context.courseId());
        lesson.setUnitId(context.unitId());
        lesson.setOwnerId(courseOwner);
        lesson.setCreatorId(context.userId());
        lesson.setModifierId(context.userId());
        lesson.set(AJEntityLesson.IS_DELETED, false);
        lesson.setTenant(context.tenant());
        String tenantRoot = context.tenantRoot();
        if (tenantRoot != null && !tenantRoot.isEmpty()) {
            lesson.setTenantRoot(tenantRoot);
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private JsonObject validateFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();
        input.fieldNames().stream().filter(key -> !AJEntityLesson.INSERTABLE_FIELDS.contains(key))
            .forEach(key -> output.put(key, "Field not allowed"));
        return output.isEmpty() ? null : output;
    }

    private JsonObject validateNullFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();
        AJEntityLesson.NOTNULL_FIELDS.stream()
            .filter(notNullField -> (input.getValue(notNullField) == null
                || input.getValue(notNullField).toString().isEmpty()))
            .forEach(notNullField -> output.put(notNullField, "Field should not be empty or null"));
        return output.isEmpty() ? null : output;
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        this.lesson.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }
}
