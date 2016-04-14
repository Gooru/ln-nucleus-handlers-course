package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ReorderCourseHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReorderCourseHandler.class);
    private final ProcessorContext context;
    private String subjectBucket;
    private static final String REORDER_PAYLOAD_ID = "id";
    private static final String REORDER_PAYLOAD_KEY_ORDER = "order";
    private static final String REORDER_PAYLOAD_SEQUENCE = "sequence_id";
    private static final String REORDER_PAYLOAD_KEY_SUBJECT_BUCKET = "subject_bucket";

    public ReorderCourseHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to reorder units");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        subjectBucket = context.request().getString(REORDER_PAYLOAD_KEY_SUBJECT_BUCKET);
        if (subjectBucket == null || subjectBucket.isEmpty()) {
            LOGGER.warn("subject bucket not present to reorder courses");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("missing subject bucket"),
                ExecutionStatus.FAILED);
        }

        if (!reorderPayloadValidator(context.request().getJsonArray(REORDER_PAYLOAD_KEY_ORDER))) {
            LOGGER.warn("Request data validation failed");
            return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(
                    new JsonObject().put("Reorder", "Data validation failed. Invalid data in request payload")),
                ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LOGGER.debug("validateRequest() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        try {
            List coursesToReorder =
                Base.firstColumn(AJEntityCourse.SELECT_COURSE_TO_REORDER, context.userId(), subjectBucket);
            JsonArray input = this.context.request().getJsonArray(REORDER_PAYLOAD_KEY_ORDER);
            if (coursesToReorder.size() != input.size()) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createInvalidRequestResponse("Course count mismatch"),
                    ExecutionResult.ExecutionStatus.FAILED);
            }

            PreparedStatement ps = Base.startBatch(AJEntityCourse.REORDER_QUERY);
            for (Object entry : input) {
                String payloadCourseId = ((JsonObject) entry).getString(REORDER_PAYLOAD_ID);
                if (!coursesToReorder.contains(UUID.fromString(payloadCourseId))) {
                    return new ExecutionResult<>(
                        MessageResponseFactory.createInvalidRequestResponse("Missing course(s)"),
                        ExecutionResult.ExecutionStatus.FAILED);
                }

                int sequenceId = ((JsonObject) entry).getInteger(AJEntityCourse.SEQUENCE_ID);
                Base.addBatch(ps, sequenceId, payloadCourseId, subjectBucket, context.userId());
            }
            Base.executeBatch(ps);
        } catch (DBException | ClassCastException e) {
            LOGGER.error("incorrect payload data type", e);
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Incorrect payload data types"),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        LOGGER.info("reordered courses for subject bucket {}", subjectBucket);
        return new ExecutionResult<>(MessageResponseFactory.createNoContentResponse(
            EventBuilderFactory.getReorderCourseEventBuilder(subjectBucket)), ExecutionStatus.SUCCESSFUL);
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
                UUID.fromString(idString);
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
