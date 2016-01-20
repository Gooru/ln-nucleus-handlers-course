package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.AJResponseJsonTransformer;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class FetchCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCourseHandler.class);
  private final ProcessorContext context;

  public FetchCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id for fetch course");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided to fetch course"),
        ExecutionStatus.FAILED);
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
    String sql = "SELECT id, title, created_at, updated_at, creator_id, original_creator_id, original_course_id, publish_date, thumbnail, audience,"
      + " metadata, taxonomy, collaborator, class_list, visible_on_profile FROM course WHERE id = ? AND is_deleted = ?";

    String unitSummarySql = "SELECT unit_id, title, sequence_id FROM course_unit where course_id = ? AND is_deleted = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId(), false);
    JsonObject body;
    if (!ajEntityCourse.isEmpty()) {
      LOGGER.info("found course for id {} : " + context.courseId());
      body = new AJResponseJsonTransformer().transformCourse(ajEntityCourse.get(0).toJson(false, AJEntityCourse.ALL_FIELDS));
      
      LazyList<AJEntityUnit> units = AJEntityUnit.findBySQL(unitSummarySql, context.courseId(), false);
      LOGGER.debug("number of units found {}", units.size());
      if(units.size() > 0) {
        body.put("unitSummary", new AJResponseJsonTransformer().transformUnitSummary(units.toJson(false, AJEntityCourse.UNIT_SUMMARY_FIELDS)));
      }
      return new ExecutionResult<>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
    } else {
      LOGGER.info("course not found {}", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
