package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FetchCourseHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchCourseHandler.class);
    private final ProcessorContext context;
    AJEntityCourse course;

    public FetchCourseHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id for fetch course");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch course"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty()) {
            LOGGER.warn("Invalid user id to fetch course");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LOGGER.debug("validateRequest() OK");
        LazyList<AJEntityCourse> ajEntityCourse =
            AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE, context.courseId(), false);
        if (!ajEntityCourse.isEmpty()) {
            LOGGER.debug("found course for id {} : " + context.courseId());
            course = ajEntityCourse.get(0);
            return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(course);
        } else {
            LOGGER.error("course not found {}", context.courseId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonObject body = new JsonObject(
            new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityCourse.ALL_FIELDS).toJson(course));
        LazyList<AJEntityUnit> units =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_SUMMARY, context.courseId(), false);
        LOGGER.debug("number of units found {}", units.size());
        if (units.size() > 0) {
            JsonArray unitSummaryArray = getLessonCountsForUnitsInCourse(units);
            body.put(AJEntityUnit.UNIT_SUMMARY, unitSummaryArray);
        }
        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private JsonArray getLessonCountsForUnitsInCourse(LazyList<AJEntityUnit> units) {
        /* 
        List<String> unitIds = new ArrayList<>();
        units.forEach(unit -> unitIds.add(unit.getString(AJEntityUnit.UNIT_ID)));

        List<Map> lessonCounts =
            Base.findAll(AJEntityLesson.SELECT_LESSON_COUNT_MULTIPLE, DbHelperUtil.toPostgresArrayString(unitIds),
                context.courseId()); */

        List<Map> lessonCounts = Base.findAll(AJEntityLesson.SELECT_LESSON_COUNT_MULTIPLE, context.courseId());
        Map<String, Integer> lessonCountByUnit = new HashMap<>();
        lessonCounts.forEach(map -> lessonCountByUnit.put(map.get(AJEntityLesson.UNIT_ID).toString(),
            Integer.valueOf(map.get(AJEntityLesson.LESSON_COUNT).toString())));
        LOGGER.debug("lesson counts: {}", lessonCountByUnit.size());
        JsonArray unitSummaryArray = new JsonArray();
        units.forEach(unit -> {
            JsonObject unitSummary = new JsonObject(
                new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityUnit.UNIT_SUMMARY_FIELDS)
                    .toJson(unit));
            Integer lessonCount = lessonCountByUnit.get(unit.get(AJEntityLesson.UNIT_ID).toString());
            unitSummary.put(AJEntityLesson.LESSON_COUNT, lessonCount != null ? lessonCount : 0);
            unitSummaryArray.add(unitSummary);
        });
        return unitSummaryArray;
    }

}
