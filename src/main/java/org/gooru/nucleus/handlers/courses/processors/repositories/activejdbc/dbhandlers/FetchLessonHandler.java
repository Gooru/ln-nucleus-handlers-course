package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
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
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch lesson"),
        ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.warn("invalid unit id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to fetch lesson"),
        ExecutionStatus.FAILED);
    }

    if (context.lessonId() == null || context.lessonId().isEmpty()) {
      LOGGER.warn("invalid lesson id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id provided to fetch lesson"),
        ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user id to fetch lesson");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {

    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("course {} not found to fetch lesson, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(), false);
    if (ajEntityUnit.isEmpty()) {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityLesson> ajEntityLesson =
      AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, context.lessonId(), context.unitId(), context.courseId(), false);
    if (ajEntityLesson.isEmpty()) {
      LOGGER.warn("Lesson {} not found, aborting", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject resultBody;
    LazyList<AJEntityLesson> ajEntityLesson =
      AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON, context.lessonId(), context.unitId(), context.courseId(), false);
    if (!ajEntityLesson.isEmpty()) {
      LOGGER.info("lesson {} found, packing into JSON", context.unitId());
      resultBody =
        new JsonObject(new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityLesson.ALL_FIELDS).toJson(ajEntityLesson.get(0)));

      LazyList<AJEntityCollection> collectionSummary = 
        AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTION_SUMMARY, context.lessonId(), context.unitId(), context.courseId(), false);

      LOGGER.debug("number of collections found for lesson {} : {}", context.lessonId(), collectionSummary.size());
      if (collectionSummary.size() > 0) {
        List<String> collectionIds = new ArrayList<>();
        collectionSummary.stream().forEach(collection -> collectionIds.add(collection.getString(AJEntityCollection.ID)));
        
        List<Map> collectionContentCount = Base.findAll(AJEntityCollection.SELECT_COLLECTION_CONTENT_COUNT, listToPostgresArrayString(collectionIds),
                context.courseId(), context.unitId(), context.lessonId());
        Map<String, Integer> resourceCountMap = new HashMap<>();
        collectionContentCount.stream()
                .filter(map -> map.get("content_format") != null && map.get("content_format").toString().equalsIgnoreCase("resource"))
                .forEach(map -> resourceCountMap.put(map.get("collection_id").toString(), Integer.valueOf(map.get("contentCount").toString())));
        
        Map<String, Integer> questionCountMap = new HashMap<>();
        collectionContentCount.stream()
                .filter(map -> map.get("content_format") != null && map.get("content_format").toString().equalsIgnoreCase("question"))
                .forEach(map -> questionCountMap.put(map.get("collection_id").toString(), Integer.valueOf(map.get("contentCount").toString())));
        
        JsonArray collectionSummaryArray = new JsonArray();
        collectionSummary.stream()
                .forEach(collection -> collectionSummaryArray.add(new JsonObject(
                        new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityCollection.COLLECTION_SUMMARY_FIELDS).toJson(collection))
                                .put("resource_count", resourceCountMap.get(collection.getString(AJEntityCollection.ID)))
                                .put("question_count", questionCountMap.get(collection.getString(AJEntityCollection.ID)))));
        resultBody.put("collectionSummary", collectionSummaryArray);
      }

      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(resultBody), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("lesson {} not found", context.lessonId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }
  
  private String listToPostgresArrayString(List<String> input) {
    int approxSize = ((input.size() + 1) * 36); // Length of UUID is around 36
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
