package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchUnitHandler implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUnitHandler.class);

    public FetchUnitHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id to fetch unit");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch unit"),
                ExecutionStatus.FAILED);
        }

        if (context.unitId() == null || context.unitId().isEmpty()) {
            LOGGER.warn("invalid unit id to fetch unit");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to fetch unit"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()) {
            LOGGER.warn("Invalid user id to fetch unit");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityCourse> ajEntityCourse =
            AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
        if (ajEntityCourse.isEmpty()) {
            LOGGER.warn("course {} not found to fetch unit, aborting", context.courseId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        LazyList<AJEntityUnit> ajEntityUnit =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(), false);
        if (ajEntityUnit.isEmpty()) {
            LOGGER.warn("Unit {} not found, aborting", context.unitId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        LazyList<AJEntityUnit> ajEntityUnits =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT, context.courseId(), context.unitId(), false);
        JsonObject resultBody;
        if (!ajEntityUnits.isEmpty()) {
            LOGGER.info("unit {} found, packing into JSON", context.unitId());
            resultBody = new JsonObject(new JsonFormatterBuilder()
                .buildSimpleJsonFormatter(false, AJEntityUnit.ALL_FIELDS).toJson(ajEntityUnits.get(0)));

            LazyList<AJEntityLesson> lessons =
                AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_SUMMARY, context.unitId(), false);
            LOGGER.debug("number of lessons found for unit {} : {}", context.unitId(), lessons.size());
            if (lessons.size() > 0) {
                List<String> lessonIds = new ArrayList<>();
                lessons.stream().forEach(lesson -> lessonIds.add(lesson.getString(AJEntityLesson.LESSON_ID)));

                List<Map> collectionCount = Base.findAll(AJEntityCollection.SELECT_COLLECTION_ASSESSMET_COUNT_BY_LESSON,
                    toPostgresArrayString(lessonIds), context.unitId(), context.courseId());
                LOGGER.debug("collection count: {}", collectionCount.size());
                Map<String, Integer> collectionCountByLesson = new HashMap<>();
                collectionCount.stream()
                    .filter(map -> map.get(AJEntityCollection.FORMAT) != null && map.get(AJEntityCollection.FORMAT)
                        .toString().equalsIgnoreCase(AJEntityCollection.FORMAT_COLLECTION))
                    .forEach(map -> collectionCountByLesson.put(map.get(AJEntityCollection.LESSON_ID).toString(),
                        Integer.valueOf(map.get(AJEntityCollection.COLLECTION_COUNT).toString())));

                Map<String, Integer> assessmentCountByLesson = new HashMap<>();
                collectionCount.stream()
                    .filter(map -> map.get(AJEntityCollection.FORMAT) != null && map.get(AJEntityCollection.FORMAT)
                        .toString().equalsIgnoreCase(AJEntityCollection.FORMAT_ASSESSMENT))
                    .forEach(map -> assessmentCountByLesson.put(map.get(AJEntityCollection.LESSON_ID).toString(),
                        Integer.valueOf(map.get(AJEntityCollection.COLLECTION_COUNT).toString())));

                JsonArray lessonSummaryArray = new JsonArray();
                lessons.stream().forEach(lesson -> {
                    JsonObject lessonSummary = new JsonObject(new JsonFormatterBuilder()
                        .buildSimpleJsonFormatter(false, AJEntityLesson.LESSON_SUMMARY_FIELDS).toJson(lesson));
                    lessonSummary.put(AJEntityCollection.COLLECTION_COUNT,
                        collectionCountByLesson.get(lesson.get(AJEntityCollection.ID).toString()));
                    lessonSummary.put(AJEntityCollection.ASSESSMENT_COUNT,
                        assessmentCountByLesson.get(lesson.get(AJEntityCollection.ID).toString()));
                    lessonSummaryArray.add(lessonSummary);
                });
                resultBody.put(AJEntityLesson.LESSON_SUMMARY, lessonSummaryArray);
            }
            return new ExecutionResult<>(MessageResponseFactory.createGetResponse(resultBody),
                ExecutionStatus.SUCCESSFUL);
        } else {
            LOGGER.error("unit {} not found", context.unitId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private String toPostgresArrayString(Collection<String> input) {
        int approxSize = ((input.size() + 1) * 36); // Length of UUID is around
                                                    // 36
                                                    // chars
        Iterator<String> it = input.iterator();
        if (!it.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder(approxSize);
        sb.append('{');
        for (;;) {
            String s = it.next();
            sb.append('"').append(s).append('"');
            if (!it.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',');
        }
    }

}
