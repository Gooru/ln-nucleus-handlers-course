package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 * Created On: 15-Mar-2017
 */
public class FetchCollectionsByCourse implements DBHandler {

    private final ProcessorContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchCollectionsByCourse.class);
    AJEntityCourse course;
    
    public FetchCollectionsByCourse(ProcessorContext context) {
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
        LazyList<AJEntityCourse> ajEntityCourse =
            AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE, context.courseId(), false);
        if (!ajEntityCourse.isEmpty()) {
            LOGGER.info("found course for id {} : " + context.courseId());
            course = ajEntityCourse.get(0);
            return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(course);
        } else {
            LOGGER.error("course not found {}", context.courseId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
     // Get all units of course
        Map<String, String> unitsTitleMap = new HashMap<>();
        LazyList<AJEntityUnit> units =
            AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_SUMMARY, context.courseId(), false);
        if (!units.isEmpty()) {
            units.forEach(unit -> {
                unitsTitleMap.put(unit.getString(AJEntityUnit.UNIT_ID), unit.getString(AJEntityUnit.TITLE));
            });
        }

        // Get all lessons of course
        Map<String, Set<String>> lessonsByUnitMap = new HashMap<>();
        Map<String, String> lessonsTitleMap = new HashMap<>();
        LazyList<AJEntityLesson> lessons =
            AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_BY_COURSE, context.courseId());
        if (!lessons.isEmpty()) {
            lessons.forEach(lesson -> {
                String lessonId = lesson.getString(AJEntityLesson.LESSON_ID);
                lessonsTitleMap.put(lessonId, lesson.getString(AJEntityLesson.TITLE));

                String unitId = lesson.getString(AJEntityLesson.UNIT_ID);
                if (lessonsByUnitMap.containsKey(unitId)) {
                    lessonsByUnitMap.get(unitId).add(lessonId);
                } else {
                    Set<String> lessonSet = new HashSet<>();
                    lessonSet.add(lessonId);
                    lessonsByUnitMap.put(unitId, lessonSet);
                }
            });
        }

        // Get all collections of course
        Map<String, JsonArray> collectionsByLessonMap = new HashMap<>();
        ;
        LazyList<AJEntityCollection> collections =
            AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTIONS_BY_COURSE, context.courseId());
        if (!collections.isEmpty()) {
            collections.forEach(collection -> {
                String lessonId = collection.getString(AJEntityCollection.LESSON_ID);

                if (collectionsByLessonMap.containsKey(lessonId)) {
                    JsonObject collectionJson = new JsonObject(new JsonFormatterBuilder()
                        .buildSimpleJsonFormatter(false, AJEntityCollection.COLLECTION_BY_COURSE_FIELDS)
                        .toJson(collection));
                    collectionsByLessonMap.get(lessonId).add(collectionJson);
                } else {
                    JsonArray collectionArray = new JsonArray();
                    collectionArray.add(new JsonObject(new JsonFormatterBuilder()
                        .buildSimpleJsonFormatter(false, AJEntityCollection.COLLECTION_BY_COURSE_FIELDS)
                        .toJson(collection)));
                    collectionsByLessonMap.put(lessonId, collectionArray);
                }
            });
        }

        JsonArray unitArray = new JsonArray();
        Set<String> unitIds = lessonsByUnitMap.keySet();
        for (String unitId : unitIds) {
            JsonObject unitArrElement =
                new JsonObject().put(AJEntityUnit.ID, unitId).put(AJEntityUnit.TITLE, unitsTitleMap.get(unitId));

            JsonArray lessonArray = new JsonArray();
            Set<String> lessonIds = lessonsByUnitMap.get(unitId);
            for (String lessonId : lessonIds) {
                JsonArray collectionArray = collectionsByLessonMap.get(lessonId);
                if (collectionArray != null) {
                    JsonObject lessonArrElement = new JsonObject().put(AJEntityLesson.ID, lessonId)
                        .put(AJEntityLesson.TITLE, lessonsTitleMap.get(lessonId));
                    lessonArrElement.put(AJEntityCollection.COLLECTIONS, (collectionArray != null ? collectionArray : new JsonArray()));
                    lessonArray.add(lessonArrElement);
                }
            }
            unitArrElement.put(AJEntityLesson.LESSONS, lessonArray);
            unitArray.add(unitArrElement);
        }

        JsonArray courseArray = new JsonArray();
        JsonObject courseArrElement = new JsonObject().put(AJEntityCourse.ID, course.getString(AJEntityCourse.ID))
            .put(AJEntityCourse.TITLE, course.getString(AJEntityCourse.TITLE));
        courseArrElement.put(AJEntityUnit.UNITS, unitArray);
        courseArray.add(courseArrElement);

        JsonObject response = new JsonObject();
        response.put(AJEntityCourse.COURSES, courseArray);

        return new ExecutionResult<>(MessageResponseFactory.createGetResponse(response), ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
