package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FetchUnitHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchUnitHandler.class);

  public FetchUnitHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id to fetch unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch unit"),
              ExecutionStatus.FAILED);
    }

    if (context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.info("invalid unit id to fetch unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to fetch unit"),
              ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to fetch unit");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId());
    if (!ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.warn("course {} is deleted, hence can't fetch unit. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which you are trying to fetch unit"),
                ExecutionStatus.FAILED);
      }

      // check whether user is either owner or collaborator
      if (!ajEntityCourse.get(0).getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
        if (!new JsonArray(ajEntityCourse.get(0).getString(AJEntityCourse.COLLABORATOR)).contains(context.userId())) {
          LOGGER.warn("user is not owner or collaborator of course to create unit. aborting");
          return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.warn("course {} not found to fetch unit, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId());
    if (!ajEntityUnit.isEmpty()) {
      if (ajEntityUnit.get(0).getBoolean(AJEntityUnit.IS_DELETED)) {
        LOGGER.info("unit {} is deleted. Aborting", context.unitId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Unit is deleted"), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.info("Unit {} not found, aborting", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    LazyList<AJEntityUnit> ajEntityUnits = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT, context.courseId(), context.unitId(), false);
    JsonObject resultBody;
    if (!ajEntityUnits.isEmpty()) {
      LOGGER.info("unit {} found, packing into JSON", context.unitId());
      resultBody = new JsonObject(new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityUnit.ALL_FIELDS).toJson(ajEntityUnits.get(0)));

      LazyList<AJEntityLesson> lessonSummary = AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_SUMMARY, context.unitId(), false);
      LOGGER.debug("number of lessons found for unit {} : {}", context.unitId(), lessonSummary.size());
      if (lessonSummary.size() > 0) {
        resultBody.put("lessonSummary", new JsonArray(
                new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityLesson.LESSON_SUMMARY_FIELDS).toJson(lessonSummary)));
      }
      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(resultBody), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.info("unit {} not found", context.unitId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
