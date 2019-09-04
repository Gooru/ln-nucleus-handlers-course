package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.LessonPlanDao;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchLessonHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchLessonHandler.class);

  public FetchLessonHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          "Invalid course id provided to fetch lesson"), ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          "Invalid unit id provided to fetch lesson"), ExecutionStatus.FAILED);
    }

    if (context.lessonId() == null || context.lessonId().isEmpty()) {
      LOGGER.warn("invalid lesson id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          "Invalid lesson id provided to fetch lesson"), ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> courses = AJEntityCourse
        .findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
    if (courses.isEmpty()) {
      LOGGER.warn("course {} not found to fetch lesson, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(
        AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(), false);
    if (ajEntityUnit.isEmpty()) {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    LazyList<AJEntityLesson> ajEntityLesson =
        AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, context.lessonId(),
            context.unitId(), context.courseId(), false);
    if (ajEntityLesson.isEmpty()) {
      LOGGER.warn("Lesson {} not found, aborting", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(courses.get(0));
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject resultBody;
    LazyList<AJEntityLesson> ajEntityLesson = AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON,
        context.lessonId(), context.unitId(), context.courseId(), false);
    if (!ajEntityLesson.isEmpty()) {
      LOGGER.info("lesson {} found, packing into JSON", context.unitId());
      resultBody = new JsonObject(
          new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityLesson.ALL_FIELDS)
              .toJson(ajEntityLesson.get(0)));

      resultBody.put(AJEntityLesson.LESSON_PLAN,
          LessonPlanDao.fetchLessonPlan(context.courseId(), context.unitId(), context.lessonId()));

      LazyList<AJEntityCollection> collectionSummary =
          AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTION_SUMMARY,
              context.lessonId(), context.unitId(), context.courseId(), false);
      LOGGER.debug("number of collections found for lesson {} : {}", context.lessonId(),
          collectionSummary.size());
      if (collectionSummary.size() > 0) {
        this.createCollectionSummary(collectionSummary, resultBody);
      }

      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(resultBody),
          ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("lesson {} not found", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }
  }

  private void findOAsInCollectionList(LazyList<AJEntityCollection> collectionSummary,
      List<String> collectionIds, List<String> oaIds) {
    collectionSummary.forEach(collection -> {
      String collectionId = collection.getString(AJEntityCollection.ID);
      if (collection.isOfflineActivity()) {
        oaIds.add(collectionId);
      }
      collectionIds.add(collectionId);
    });
  }

  private void createCollectionSummary(LazyList<AJEntityCollection> collectionSummary,
      JsonObject resultBody) {
    List<String> collectionIds = new ArrayList<>();
    List<String> oaIds = new ArrayList<>();
    findOAsInCollectionList(collectionSummary, collectionIds, oaIds);
    List<Map> collectionContentCount = getCollectionContentCount(collectionIds);
    Map<String, Integer> oaCountMap = this.getOACount(oaIds);
    Map<String, Integer> resourceCountMap = new HashMap<>();
    Map<String, Integer> questionCountMap = new HashMap<>();
    this.getCollectionResourceAndQuestionCount(collectionContentCount, resourceCountMap,
        questionCountMap);
    Map<String, Integer> oeQuestionCountMap = new HashMap<>();
    this.getCollectionOEQuestionCount(collectionIds, oeQuestionCountMap);
    JsonArray collectionSummaryArray = new JsonArray();
    collectionSummary.forEach(collection -> {
      String collectionId = collection.getString(AJEntityCollection.ID);
      Integer resourceCount = resourceCountMap.get(collectionId);
      Integer questionCount = questionCountMap.get(collectionId);
      Integer oeQuestionCount = oeQuestionCountMap.get(collectionId);
      Integer taskCount = oaCountMap.get(collectionId);
      collectionSummaryArray.add(new JsonObject(new JsonFormatterBuilder()
          .buildSimpleJsonFormatter(false, AJEntityCollection.COLLECTION_SUMMARY_FIELDS)
          .toJson(collection))
              .put(AJEntityContent.RESOURCE_COUNT, resourceCount != null ? resourceCount : 0)
              .put(AJEntityContent.QUESTION_COUNT, questionCount != null ? questionCount : 0)
              .put(AJEntityContent.OE_QUESTION_COUNT, oeQuestionCount != null ? oeQuestionCount : 0)
              .put(AJEntityContent.OA_TASK_COUNT, taskCount != null ? taskCount : 0));
    });

    resultBody.put(AJEntityCollection.COLLECTION_SUMMARY, collectionSummaryArray);
  }

  private Map<String, Integer> getOACount(List<String> oaIds) {
    Map<String, Integer> oaCountMap = new HashMap<>();
    if (!oaIds.isEmpty()) {
      String oaArrayString = DbHelperUtil.toPostgresArrayString(oaIds);
      List<Map> oaCount = Base.findAll(AJEntityContent.FETCH_TASK_COUNT_BY_OA, oaArrayString);
      oaCount.forEach(map -> oaCountMap.put(map.get(AJEntityContent.COLLECTION_ID).toString(),
          Integer.valueOf(map.get(AJEntityContent.OA_TASK_COUNT).toString())));
    }
    return oaCountMap;
  }

  private List<Map> getCollectionContentCount(List<String> collectionIds) {
    String collectionArrayString = DbHelperUtil.toPostgresArrayString(collectionIds);
    List<Map> collectionContentCount =
        Base.findAll(AJEntityContent.SELECT_CONTENT_COUNT_BY_COLLECTION, collectionArrayString,
            context.courseId(), context.unitId(), context.lessonId());
    return collectionContentCount;

  }

  private void getCollectionResourceAndQuestionCount(List<Map> collectionContentCount,
      Map<String, Integer> resourceCountMap, Map<String, Integer> questionCountMap) {

    collectionContentCount.forEach(map -> {
      String contentFormat = map.get(AJEntityContent.CONTENT_FORMAT).toString();
      if (isResourceContent(contentFormat)) {
        resourceCountMap.put(map.get(AJEntityContent.COLLECTION_ID).toString(),
            Integer.valueOf(map.get(AJEntityContent.CONTENT_COUNT).toString()));
      } else if (isQuestionContent(contentFormat)) {
        questionCountMap.put(map.get(AJEntityContent.COLLECTION_ID).toString(),
            Integer.valueOf(map.get(AJEntityContent.CONTENT_COUNT).toString()));
      }
    });
  }

  private void getCollectionOEQuestionCount(List<String> collectionIds,
      Map<String, Integer> oeQuestionCountMap) {
    String collectionArrayString = DbHelperUtil.toPostgresArrayString(collectionIds);
    List<Map> oeQuestionCountFromDB = Base.findAll(AJEntityContent.SELECT_OE_QUESTION_COUNT,
        collectionArrayString, context.courseId(), context.unitId(), context.lessonId());
    oeQuestionCountFromDB
        .forEach(map -> oeQuestionCountMap.put(map.get(AJEntityContent.COLLECTION_ID).toString(),
            Integer.valueOf(map.get(AJEntityContent.OE_QUESTION_COUNT).toString())));
  }

  private boolean isResourceContent(String contentFormat) {
    return (contentFormat != null
        && contentFormat.equalsIgnoreCase(AJEntityContent.CONTENT_FORMAT_RESOURCE));
  }

  private boolean isQuestionContent(String contentFormat) {
    return (contentFormat != null
        && contentFormat.equalsIgnoreCase(AJEntityContent.CONTENT_FORMAT_QUESTION));
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
