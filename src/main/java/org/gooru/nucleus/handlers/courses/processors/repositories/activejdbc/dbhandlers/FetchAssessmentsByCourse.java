package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 09-Mar-2017
 */
public class FetchAssessmentsByCourse implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchAssessmentsByCourse.class);
  AJEntityCourse course;

  public FetchAssessmentsByCourse(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id for fetch course");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid course id provided to fetch course"),
          ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user id to fetch course");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
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
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // Get all units of course
    Map<String, AJEntityUnit> unitsTitleMap = new HashMap<>();
    LazyList<AJEntityUnit> units =
        AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_SUMMARY, context.courseId(), false);
    if (!units.isEmpty()) {
      units.forEach(unit -> {
        unitsTitleMap.put(unit.getString(AJEntityUnit.UNIT_ID), unit);
      });
    }

    // Get all lessons of course
    Map<String, Set<String>> lessonsByUnitMap = new HashMap<>();
    Map<String, AJEntityLesson> lessonsTitleMap = new HashMap<>();
    LazyList<AJEntityLesson> lessons =
        AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_BY_COURSE, context.courseId());
    if (!lessons.isEmpty()) {
      lessons.forEach(lesson -> {
        String lessonId = lesson.getString(AJEntityLesson.LESSON_ID);
        lessonsTitleMap.put(lessonId, lesson);

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

    // Get all assessments of course
    Map<String, JsonArray> assessmentsByLessonMap = new HashMap<>();
    ;
    LazyList<AJEntityCollection> assessments =
        AJEntityCollection
            .findBySQL(AJEntityCollection.SELECT_ASSESSMENTS_BY_COURSE, context.courseId());
    if (!assessments.isEmpty()) {
      assessments.forEach(assessment -> {
        String lessonId = assessment.getString(AJEntityCollection.LESSON_ID);

        if (assessmentsByLessonMap.containsKey(lessonId)) {
          JsonObject assessmentJson = new JsonObject(new JsonFormatterBuilder()
              .buildSimpleJsonFormatter(false, AJEntityCollection.ASSESSMENT_BY_COURSE_FIELDS)
              .toJson(assessment));
          assessmentsByLessonMap.get(lessonId).add(assessmentJson);
        } else {
          JsonArray assessmentArray = new JsonArray();
          assessmentArray.add(new JsonObject(new JsonFormatterBuilder()
              .buildSimpleJsonFormatter(false, AJEntityCollection.ASSESSMENT_BY_COURSE_FIELDS)
              .toJson(assessment)));
          assessmentsByLessonMap.put(lessonId, assessmentArray);
        }
      });
    }

    JsonArray unitArray = new JsonArray();
    Set<String> unitIds = lessonsByUnitMap.keySet();
    for (String unitId : unitIds) {
      AJEntityUnit unitFromMap = unitsTitleMap.get(unitId);
      JsonObject unitArrElement = new JsonObject().put(AJEntityUnit.ID, unitId)
          .put(AJEntityUnit.TITLE, unitFromMap.getString(AJEntityUnit.TITLE))
          .put(AJEntityUnit.SEQUENCE_ID, unitFromMap.getInteger(AJEntityUnit.SEQUENCE_ID));

      JsonArray lessonArray = new JsonArray();
      Set<String> lessonIds = lessonsByUnitMap.get(unitId);
      for (String lessonId : lessonIds) {
        JsonArray assessmentArray = assessmentsByLessonMap.get(lessonId);
        AJEntityLesson lessonFromMap = lessonsTitleMap.get(lessonId);
        JsonObject lessonArrElement = new JsonObject().put(AJEntityLesson.ID, lessonId)
            .put(AJEntityLesson.TITLE, lessonFromMap.getString(AJEntityLesson.TITLE))
            .put(AJEntityLesson.SEQUENCE_ID, lessonFromMap.getInteger(AJEntityLesson.SEQUENCE_ID));
        lessonArrElement.put(AJEntityCollection.ASSESSMENTS,
            (assessmentArray != null ? assessmentArray : new JsonArray()));
        lessonArray.add(lessonArrElement);
      }
      unitArrElement.put(AJEntityLesson.LESSONS, lessonArray);
      unitArray.add(unitArrElement);
    }

    JsonArray courseArray = new JsonArray();
    JsonObject courseArrElement = new JsonObject()
        .put(AJEntityCourse.ID, course.getString(AJEntityCourse.ID))
        .put(AJEntityCourse.TITLE, course.getString(AJEntityCourse.TITLE));
    courseArrElement.put(AJEntityUnit.UNITS, unitArray);
    courseArray.add(courseArrElement);

    JsonObject response = new JsonObject();
    response.put(AJEntityCourse.COURSES, courseArray);

    return new ExecutionResult<>(MessageResponseFactory.createGetResponse(response),
        ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
