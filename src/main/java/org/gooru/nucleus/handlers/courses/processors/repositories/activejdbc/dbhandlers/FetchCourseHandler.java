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

/*    
    {"id":"2c9eeda8-4d6b-4a80-898b-77d557e93e2b",
        "title":"Navigate Math (Remix)",
        "description":"Navigate Math is an outcomes-proven course with hundreds of students across Leadership Public Schools' high school campuses growing nearly 3 years in mathematics in 1 school year as measured by NWEA MAP. Navigate Math is an intervention course and approach that accelerates academic growth and empowers students to direct their own learning. Navigate Math helps students develop the foundational knowledge, skills, and thought processes required to be successful in pre-Algebra and Algebra.",
        "owner_id":"df956d5f-b7b2-43ae-98a1-c90a12eacaf9",
        "creator_id":"df956d5f-b7b2-43ae-98a1-c90a12eacaf9",
        "original_creator_id":"3dc4f064-609b-4414-836e-1f16bcfd0b8b",
        "modifier_id":"df956d5f-b7b2-43ae-98a1-c90a12eacaf9",
        "original_course_id":"7b58ac43-075b-46c4-a7f4-a1ce2b346e85",
        "publish_status":"unpublished",
        "publish_date":null,
        "thumbnail":"a4dfc8ad-eb44-47cf-826b-0d9e213a9f85.png",
        "metadata":null,
        "taxonomy":null,
        "collaborator":null,
        "visible_on_profile":false,
        "created_at":"2016-11-07T02:14:38Z",
        "updated_at":"2018-04-18T08:56:26Z",
        "sequence_id":82,
        "subject_bucket":"CCSS.K12.MA",
        "license":null,"creator_system":null,
        "use_case":"Navigate Math is a support math course that accompanies the core math content for grades 7-9 preparing students for the rigor of Algebra by focusing on skill building and changing the way students think about math. Gooru allows us to present a variety of resources and allows students to go at their own pace. Our students are on Gooru for the majority of class. With Gooru and our one-to-one Chromebook program, students have 24-hour access to the content and can choose their own path for learning.",
        "version":"3.0",
        "aggregated_taxonomy":null,
        "unit_summary":[
             {"unit_id":"1aab410b-4d1d-477f-8218-d06f2b57d03d","title":"Order of Operations","sequence_id":1,"lesson_count":16},
             {"unit_id":"06a05ffe-d16b-4ad4-8179-bc1ca2348f9d","title":"Integers & Absolute Value","sequence_id":2,"lesson_count":19},
             {"unit_id":"fda1d831-5cc8-4fd4-8fd6-321b7b299bcf","title":"Fraction Types","sequence_id":3,"lesson_count":20},
             {"unit_id":"f8886f43-aff0-4180-afe2-105d112f911b","title":"Fraction Operations","sequence_id":4,"lesson_count":20},
             {"unit_id":"5b6c16fa-16a6-4072-8047-cb58c563943e","title":"Decimal Operations","sequence_id":5,"lesson_count":20},
             {"unit_id":"b9fc7631-9090-440f-a1f6-4c65dbfb6a98","title":"Conversions","sequence_id":6,"lesson_count":18},
             {"unit_id":"9a34e893-e7d0-4cc5-ae6d-fd676fc06e61","title":"Ratios & Proportions","sequence_id":7,"lesson_count":16},
             {"unit_id":"3ded6016-1a0b-4fa7-940d-6753cabefc4f","title":"Academic Techniques","sequence_id":8,"lesson_count":4},
             {"unit_id":"0c51b9bf-e4c7-4e2a-ab8e-a84e3cacd5cf","title":"Ownership","sequence_id":9,"lesson_count":4},
             {"unit_id":"596bf13a-94b0-45b7-9feb-d0d3cdc83d27","title":"Mental Math","sequence_id":10,"lesson_count":5},
             {"unit_id":"62c51c31-8724-476c-a783-e5c169bd35e9","title":"Teacher Resources","sequence_id":11,"lesson_count":3}]
    }
*/

    private JsonObject getCourseData() {
        JsonObject obj = new JsonObject();
        obj.put("title", "Navigate Math (Remix)");
        obj.put("description", "Navigate Math is an outcomes-proven course with hundreds of students across Leadership Public Schools' high school campuses growing nearly 3 years in mathematics in 1 school year as measured by NWEA MAP. Navigate Math is an intervention course and approach that accelerates academic growth and empowers students to direct their own learning. Navigate Math helps students develop the foundational knowledge, skills, and thought processes required to be successful in pre-Algebra and Algebra.");
        obj.put("owner_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("creator_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("original_creator_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("modifier_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("original_course_id", "7b58ac43-075b-46c4-a7f4-a1ce2b346e85");
        obj.put("publish_status", "unpublished");
        obj.putNull("publish_date");
        obj.put("thumbnail", "a4dfc8ad-eb44-47cf-826b-0d9e213a9f85.png");
        obj.putNull("metadata");
        obj.putNull("taxonomy");
        obj.putNull("collaborator");
        obj.put("visible_on_profile", (Boolean)false);
        obj.put("created_at", "2016-11-07T02:14:38Z");
        obj.put("updated_at", "2018-04-18T08:56:26Z");
        obj.put("sequence_id", 82);
        obj.put("subject_bucket", "CCSS.K12.MA");
        obj.putNull("license");
        obj.putNull("creator_system");
        obj.put("use_case", "Navigate Math is a support math course that accompanies the core math content for grades 7-9 preparing students for the rigor of Algebra by focusing on skill building and changing the way students think about math. Gooru allows us to present a variety of resources and allows students to go at their own pace. Our students are on Gooru for the majority of class. With Gooru and our one-to-one Chromebook program, students have 24-hour access to the content and can choose their own path for learning.");
        obj.put("version", "3.0");
        obj.putNull("aggregated_taxonomy");

        JsonArray unitArr = new JsonArray();
        JsonObject unit = new JsonObject();
        unit.put("unit_id", "1aab410b-4d1d-477f-8218-d06f2b57d03d");
        unit.put("title", "Order of Operations");
        unit.put("sequence_id", 1);
        unit.put("lesson_count", 16);

        unitArr.add(unit);
        unit = new JsonObject();
        unit.put("unit_id", "06a05ffe-d16b-4ad4-8179-bc1ca2348f9d");
        unit.put("title", "Integers & Absolute Value");
        unit.put("sequence_id", 2);
        unit.put("lesson_count", 19);

        unitArr.add(unit);
        unit = new JsonObject();
        unit.put("unit_id", "fda1d831-5cc8-4fd4-8fd6-321b7b299bcf");
        unit.put("title", "Fraction Types");
        unit.put("sequence_id", 3);
        unit.put("lesson_count", 20);

        unitArr.add(unit);
        unit = new JsonObject();
        unit.put("unit_id", "f8886f43-aff0-4180-afe2-105d112f911b");
        unit.put("title", "Fraction Operations");
        unit.put("sequence_id", 4);
        unit.put("lesson_count", 20);

        unitArr.add(unit);

        obj.put("unit_summary", unitArr);

        return obj;
    } 

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonObject body = getCourseData();
        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
/*
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
*/
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
