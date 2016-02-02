package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCourseHandler.class);
  private final ProcessorContext context;

  public FetchCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.warn("invalid course id for fetch course");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch course"),
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
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE, context.courseId(), false);
    JsonObject body;
    if (!ajEntityCourse.isEmpty()) {
      LOGGER.info("found course for id {} : " + context.courseId());
      body = new JsonObject(new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityCourse.ALL_FIELDS).toJson(ajEntityCourse.get(0)));

      LazyList<AJEntityUnit> units = AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_SUMMARY, context.courseId(), false);
      LOGGER.debug("number of units found {}", units.size());
      if (units.size() > 0) {
        body.put("unitSummary",
          new JsonArray(new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityUnit.UNIT_SUMMARY_FIELDS).toJson(units)));
      }
      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.error("course not found {}", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
