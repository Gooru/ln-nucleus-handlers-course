package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.create.LessonPlanCreateCommand;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.delete.LessonPlanDeleteCommand;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.update.LessonPlanUpdateCommand;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.ModelErrorFormatter;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;



public final class LessonPlanDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(LessonPlanDao.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");


  private LessonPlanDao() {
    throw new AssertionError();
  }

  private static final Set<String> EDITABLE_FIELDS = new HashSet<>(
      Arrays.asList(AJEntityLessonPlan.DESCRIPTION, AJEntityLessonPlan.GUIDING_QUESTIONS,
          AJEntityLessonPlan.PRIOR_KNOWLEDGE, AJEntityLessonPlan.ANTICIPATED_STRUGGLES,
          AJEntityLessonPlan.REFERENCE_LINKS, AJEntityLessonPlan.PACING_GUIDE_IN_HRS,
          AJEntityLessonPlan.START_WEEK, AJEntityLessonPlan.SESSIONS));

  private static final Set<String> CREATABLE_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.DESCRIPTION,
          AJEntityLessonPlan.GUIDING_QUESTIONS, AJEntityLessonPlan.PRIOR_KNOWLEDGE,
          AJEntityLessonPlan.ANTICIPATED_STRUGGLES, AJEntityLessonPlan.REFERENCE_LINKS,
          AJEntityLessonPlan.PACING_GUIDE_IN_HRS, AJEntityLessonPlan.START_WEEK));

  private static final Set<String> MANDATORY_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.DESCRIPTION));

  private static final Set<String> CREATABLE_SESSION_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.TITLE, AJEntityLessonPlan.DESCRIPTION,
          AJEntityLessonPlan.DURATION, AJEntityLessonPlan.TEACHER_CONTENTS,
          AJEntityLessonPlan.STUDENT_CONTENTS));

  private static final Set<String> CREATABLE_SESSION_CONTENT_FIELDS = new HashSet<>(
      Arrays.asList(AJEntityLessonPlan.CONTENT_ID, AJEntityLessonPlan.CONTENT_FORMAT));

  private static final Set<String> MANDATORY_SESSION_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.TITLE));

  private static final Set<String> MANDATORY_SESSION_CONTENT_FIELDS = new HashSet<>(
      Arrays.asList(AJEntityLessonPlan.CONTENT_ID, AJEntityLessonPlan.CONTENT_FORMAT));

  private static final List<String> RESPONSE_FIELDS = Arrays.asList(AJEntityLessonPlan.DESCRIPTION,
      AJEntityLessonPlan.GUIDING_QUESTIONS, AJEntityLessonPlan.PRIOR_KNOWLEDGE,
      AJEntityLessonPlan.ANTICIPATED_STRUGGLES, AJEntityLessonPlan.REFERENCE_LINKS,
      AJEntityLessonPlan.PACING_GUIDE_IN_HRS, AJEntityLessonPlan.START_WEEK,
      AJEntityLessonPlan.SESSIONS, AJEntityLessonPlan.LESSON_PLAN_ID);
  public static final String OFFLINE_ACTIVITY = "offline-activity";
  private static final Set<String> CONTENT_TYPES =
      new HashSet<>(Arrays.asList("question", "resource"));
  private static final Set<String> COLLECTION_CONTENT_TYPES =
      new HashSet<>(Arrays.asList("collection", "assessment"));
  private static final Set<String> COLLECTION_TYPES = new HashSet<>(Arrays.asList("assessment",
      "collection", "assessment-external", "collection-external", OFFLINE_ACTIVITY));
  private static final String UNDERSCORE = "_";


  public static final String SELECT_LESSON_PLAN_TO_VALIDATE =
      "SELECT id, lesson_id, unit_id, course_id FROM lesson_plan WHERE id = ?::bigint AND lesson_id = ?::uuid "
          + "AND unit_id = ?::uuid AND course_id = ?::uuid";

  private static final String FETCH_LESSON_PLAN =
      "SELECT id, lesson_id, unit_id, course_id, description, guiding_questions, prior_knowledge, anticipated_struggles, reference_links, "
          + "pacing_guide_in_hrs, start_week, sessions FROM lesson_plan WHERE  course_id = ?::uuid AND unit_id = ?::uuid AND lesson_id = ?::uuid";

  private static final String FETCH_COLLECTIONS =
      "select id AS content_id, course_id, unit_id, lesson_id, title, thumbnail, format as content_format, subformat as content_subformat FROM collection WHERE"
          + " id = ANY(?::uuid[]) AND is_deleted = false";

  private static final String FETCH_CONTENTS =
      "select id AS content_id, course_id, unit_id, lesson_id, title, thumbnail, content_format, content_subformat FROM content WHERE"
          + " id = ANY(?::uuid[]) AND is_deleted = false";

  private static final String FETCH_TASK_COUNT_BY_OA =
      "SELECT count(id) as task_count, oa_id AS content_id FROM oa_tasks WHERE oa_id = ANY(?::uuid[]) GROUP BY oa_id";

  private static final String FETCH_CONTENT_COUNT_BY_COLLECTION =
      "SELECT count(id) as content_count, content_format, concat(content_format, '_', collection_id) AS content_id FROM content WHERE collection_id = ANY(?::uuid[]) AND  is_deleted = false GROUP BY collection_id, content_format";

  private static final String FETCH_OE_QUESTION_COUNT =
      "SELECT count(id) as oe_question_count, collection_id AS content_id FROM content WHERE collection_id = ANY(?::uuid[]) "
          + "AND is_deleted = false AND content_format = 'question' AND "
          + "content_subformat = 'open_ended_question' GROUP BY collection_id";

  private static final String LESSON_PLAN_EXISTS =
      "SELECT EXISTS (SELECT 1 FROM lesson_plan WHERE course_id = ?::uuid AND unit_id = ?::uuid AND lesson_id = ?::uuid)";

  public static final List<String> FETCH_FIELD_LIST =
      Arrays.asList("id", "description", "login_required", "course_id", "unit_id", "lesson_id");

  public static FieldSelector editFieldSelector() {
    return () -> Collections.unmodifiableSet(EDITABLE_FIELDS);
  }

  public static FieldSelector createSessionContentFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_SESSION_CONTENT_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_SESSION_CONTENT_FIELDS);
      }
    };
  }

  public static FieldSelector createSessionFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_SESSION_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_SESSION_FIELDS);
      }
    };
  }

  public static FieldSelector createFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_FIELDS);
      }
    };
  }

  public static ValidatorRegistry getValidatorRegistry() {
    return new LessonPlanValidationRegistry();
  }

  public static ValidatorRegistry getSessionValidatorRegistry() {
    return new LessonPlanSessionValidationRegistry();
  }

  public static ValidatorRegistry getSessionTeacherContentValidatorRegistry() {
    return new LessonPlanSessionTeacherContentValidationRegistry();
  }

  public static ValidatorRegistry getSessionStudentContentValidatorRegistry() {
    return new LessonPlanSessionStudentContentValidationRegistry();
  }

  public static ConverterRegistry getConverterRegistry() {
    return new LessonPlanConverterRegistry();
  }

  private static class LessonPlanValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorRegistry.get(fieldName);
    }
  }

  private static class LessonPlanSessionValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorSessionRegistry.get(fieldName);
    }
  }

  private static class LessonPlanSessionTeacherContentValidationRegistry
      implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorSessionTeacherContentRegistry.get(fieldName);
    }
  }

  private static class LessonPlanSessionStudentContentValidationRegistry
      implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorSessionStudentContentRegistry.get(fieldName);
    }
  }


  private static class LessonPlanConverterRegistry implements ConverterRegistry {

    @Override
    public FieldConverter lookupConverter(String fieldName) {
      return AJEntityLessonPlan.converterRegistry.get(fieldName);
    }
  }

  public static boolean checkLessonPlanExists(String courseId, String unitId, String lessonId) {
    Object objExists = Base.firstCell(LESSON_PLAN_EXISTS, courseId, unitId, lessonId);
    return Boolean.valueOf(String.valueOf(objExists));
  }

  public static String createLessonPlan(LessonPlanCreateCommand command) {
    AJEntityLessonPlan lessonPlan = new AJEntityLessonPlan();
    autoPopulateFields(command, lessonPlan);
    new EntityBuilder<AJEntityLessonPlan>() {}.build(lessonPlan, command.requestPayload(),
        LessonPlanDao.getConverterRegistry());
    boolean result = lessonPlan.save();
    if (!result) {
      LOGGER.error("Lesson Plan  creation failed for user '{}'", command.getUserId());
      if (lessonPlan.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(lessonPlan)));
      }
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInternalErrorResponse());
    }
    updateCourseUpdatedAt(command.getCourse());
    return lessonPlan.getId().toString();
  }

  public static void validateCourseIsPremium(AJEntityCourse course, String courseId) {
    if (!course.isPremium()) {
      LOGGER.warn("Course: '{}' is not premium", courseId);
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("lesson.plan.only.for.navigator.course")));

    }
  }

  public static void updateLessonPlan(LessonPlanUpdateCommand command) {
    AJEntityLessonPlan lessonPlan = command.getLessonPlan();
    new EntityBuilder<AJEntityLessonPlan>() {}.build(lessonPlan, command.requestPayload(),
        LessonPlanDao.getConverterRegistry());
    boolean result = lessonPlan.save();
    if (!result) {
      LOGGER.error("Lesson Plan  updation failed for user '{}'", command.getUserId());
      if (lessonPlan.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(lessonPlan)));
      }
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInternalErrorResponse());
    }
  }

  public static void deleteLessonPlan(LessonPlanDeleteCommand command) {
    AJEntityLessonPlan lessonPlan = command.getLessonPlan();
    boolean result = lessonPlan.delete();
    if (!result) {
      LOGGER.error("Lesson Plan  deletion failed for user '{}'", command.getUserId());
      if (lessonPlan.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(lessonPlan)));
      }
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInternalErrorResponse());
    }
  }


  public static JsonObject fetchLessonPlan(String courseId, String unitId, String lessonId) {
    LazyList<AJEntityLessonPlan> lessonplans =
        AJEntityLessonPlan.findBySQL(FETCH_LESSON_PLAN, courseId, unitId, lessonId);
    JsonObject result = null;
    if (lessonplans != null & !lessonplans.isEmpty()) {
      AJEntityLessonPlan lessonPlan = lessonplans.get(0);
      if (lessonPlan != null) {
        result = new JsonObject(new JsonFormatterBuilder()
            .buildSimpleJsonFormatter(false, RESPONSE_FIELDS).toJson(lessonPlan));
        JsonArray sessions = result.getJsonArray(AJEntityLessonPlan.SESSIONS, null);
        populateLessonPlanSessionContentFields(lessonPlan, sessions);
      }
    }
    return result;

  }

  private static void updateCourseUpdatedAt(AJEntityCourse course) {
    course.setTimestamp(AJEntityCourse.UPDATED_AT, new Timestamp(System.currentTimeMillis()));
    boolean result = course.save();
    if (!result) {
      LOGGER.error("Course with id '{}' failed to save modified time stamp", course.getId());
      if (course.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(course)));
      }
    }
  }

  private static void autoPopulateFields(LessonPlanCreateCommand command,
      AJEntityLessonPlan lessonPlan) {
    lessonPlan.setCourseId(command.getCourseId());
    lessonPlan.setUnitId(command.getUnitId());
    lessonPlan.setLessonId(command.getLessonId());
  }

  @SuppressWarnings("rawtypes")
  private static void populateLessonPlanSessionContentFields(AJEntityLessonPlan lessonPlan,
      JsonArray sessions) {
    List<String> collectionIds = new ArrayList<>();
    List<String> oaIds = new ArrayList<>();
    List<String> contentIds = new ArrayList<>();
    if (sessions != null) {
      contentIdsListByFormat(sessions, collectionIds, oaIds, contentIds);
      Map<String, Map<Object, Object>> collectionsMap = new HashMap<>();
      Map<String, Map<Object, Object>> collectionsCountMap = new HashMap<>();
      Map<String, Map<Object, Object>> oaCountMap = new HashMap<>();
      Map<String, Map<Object, Object>> collectionsOECountMap = new HashMap<>();
      Map<String, Map<Object, Object>> contentsMap = new HashMap<>();

      if (!collectionIds.isEmpty()) {
        String collectionArrayString = DbHelperUtil.toPostgresArrayString(collectionIds);
        List<Map> collections = Base.findAll(FETCH_COLLECTIONS, collectionArrayString);
        mapContentWithIdRef(collections, collectionsMap);
        List<Map> collectionsContentCount =
            Base.findAll(FETCH_CONTENT_COUNT_BY_COLLECTION, collectionArrayString);
        mapContentWithIdRef(collectionsContentCount, collectionsCountMap);
        List<Map> collectionsOECount = Base.findAll(FETCH_OE_QUESTION_COUNT, collectionArrayString);
        mapContentWithIdRef(collectionsOECount, collectionsOECountMap);
      }

      if (!oaIds.isEmpty()) {
        String oaArrayString = DbHelperUtil.toPostgresArrayString(oaIds);
        List<Map> oaTaskCount = Base.findAll(FETCH_TASK_COUNT_BY_OA, oaArrayString);
        mapContentWithIdRef(oaTaskCount, oaCountMap);
      }

      if (!contentIds.isEmpty()) {
        String contentArrayString = DbHelperUtil.toPostgresArrayString(contentIds);
        List<Map> contents = Base.findAll(FETCH_CONTENTS, contentArrayString);
        mapContentWithIdRef(contents, contentsMap);
      }
      mergeSessionContentSummary(sessions, collectionsMap, collectionsCountMap, oaCountMap,
          contentsMap, collectionsOECountMap);

    }

  }

  private static void contentIdsListByFormat(JsonArray sessions, List<String> collectionIds,
      List<String> oaIds, List<String> contentIds) {
    sessions.forEach(sessionData -> {
      JsonObject session = (JsonObject) sessionData;
      JsonArray contents = session.getJsonArray(AJEntityLessonPlan.TEACHER_CONTENTS, null);
      if (contents != null && !contents.isEmpty()) {
        contents.forEach(contentData -> {
          JsonObject content = (JsonObject) contentData;
          String contentFormat = content.getString(AJEntityLessonPlan.CONTENT_FORMAT);
          String contentId = content.getString(AJEntityLessonPlan.CONTENT_ID);
          if (COLLECTION_TYPES.contains(contentFormat)) {
            collectionIds.add(contentId);
            if (isOA(contentFormat)) {
              oaIds.add(contentId);
            }
          } else if (CONTENT_TYPES.contains(contentFormat)) {
            contentIds.add(contentId);
          }
        });
      }
    });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void mapContentWithIdRef(List<Map> contents,
      Map<String, Map<Object, Object>> contentsMap) {
    if (contents != null) {
      contents.forEach(content -> {
        Map<Object, Object> contentMap = new HashMap<>();
        content.keySet().forEach(key -> {
          contentMap.put(key, content.get(key));
        });
        String id = content.get(AJEntityLessonPlan.CONTENT_ID).toString();
        contentsMap.put(id, contentMap);
      });
    }
  }

  private static void mergeSessionContentSummary(JsonArray sessions,
      Map<String, Map<Object, Object>> collectionsMap,
      Map<String, Map<Object, Object>> collectionsCountMap,
      Map<String, Map<Object, Object>> oaCountMap, Map<String, Map<Object, Object>> contentsMap,
      Map<String, Map<Object, Object>> collectionsOECountMap) {
    sessions.forEach(sessionData -> {
      JsonObject session = (JsonObject) sessionData;
      JsonArray contents = session.getJsonArray(AJEntityLessonPlan.TEACHER_CONTENTS, null);
      if (contents != null && !contents.isEmpty()) {
        JsonArray contentSummary = new JsonArray();
        contents.forEach(contentData -> {
          JsonObject content = (JsonObject) contentData;
          String contentFormat = content.getString(AJEntityLessonPlan.CONTENT_FORMAT);
          String contentId = content.getString(AJEntityLessonPlan.CONTENT_ID);
          if (COLLECTION_TYPES.contains(contentFormat)) {
            Map<Object, Object> collectionMap = collectionsMap.get(contentId);
            if (isOA(contentFormat)) {
              Map<Object, Object> offlineActivityCountMap = oaCountMap.get(contentId);
              collectionMap.put(AJEntityContent.OA_TASK_COUNT,
                  offlineActivityCountMap != null
                      ? offlineActivityCountMap.get(AJEntityContent.OA_TASK_COUNT)
                      : 0);
            } else if (COLLECTION_CONTENT_TYPES.contains(contentFormat)) {
              String resourceCountId =
                  AJEntityContent.CONTENT_FORMAT_RESOURCE + UNDERSCORE + contentId;
              String questionCountId =
                  AJEntityContent.CONTENT_FORMAT_QUESTION + UNDERSCORE + contentId;
              Map<Object, Object> collectionResourceCountMap =
                  collectionsCountMap.get(resourceCountId);
              Map<Object, Object> collectionQuestionCountMap =
                  collectionsCountMap.get(questionCountId);
              collectionMap.put(AJEntityContent.RESOURCE_COUNT,
                  collectionResourceCountMap != null
                      ? collectionResourceCountMap.get(AJEntityContent.CONTENT_COUNT)
                      : 0);
              collectionMap.put(AJEntityContent.QUESTION_COUNT,
                  collectionQuestionCountMap != null
                      ? collectionQuestionCountMap.get(AJEntityContent.CONTENT_COUNT)
                      : 0);
              Map<Object, Object> collectionOECountMap = collectionsOECountMap.get(contentId);

              collectionMap.put(AJEntityContent.OE_QUESTION_COUNT,
                  collectionOECountMap != null
                      ? collectionOECountMap.get(AJEntityContent.OE_QUESTION_COUNT)
                      : 0);
            }

            if (collectionMap != null) {
              contentSummary.add(collectionMap);
            }
          } else if (CONTENT_TYPES.contains(contentFormat)) {
            Map<Object, Object> contentMap = contentsMap.get(contentId);
            if (contentMap != null) {
              contentSummary.add(contentMap);
            }
          }
        });
        session.put(AJEntityLessonPlan.TEACHER_CONTENTS, contentSummary);
      }
    });

  }

  private static boolean isOA(String contentFormat) {
    return contentFormat != null && contentFormat.equalsIgnoreCase(OFFLINE_ACTIVITY);
  }

}
