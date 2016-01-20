package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.AJResponseJsonTransformer;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.AJValidatorBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    if(context.unitId() == null || context.unitId().isEmpty()) {
      LOGGER.info("invalid unit id to fetch unit");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid unit id provided to fetch unit"), ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    AJValidatorBuilder ajValidatorBuilder = new AJValidatorBuilder();
    ExecutionResult<MessageResponse> executionResult = ajValidatorBuilder.buildCourseValidator(context, LOGGER).checkIsDeleted();
    if(!executionResult.continueProcessing()) {
      return executionResult;
    }
    
    return ajValidatorBuilder.buildUnitValidator(context, LOGGER).checkIsDeleted();
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    String fetchUnitSql = "SELECT course_id, unit_id, title, created_at, updated_at, creator_id, modifier_id, original_creator_id, original_unit_id,"
            + " big_ideas, essential_questions, metadata, taxonomy, sequence_id, is_deleted FROM course_unit WHERE course_id = ? AND unit_id = ? AND is_deleted = ?";
    LazyList<AJEntityUnit> ajEntityUnits = AJEntityUnit.findBySQL(fetchUnitSql, context.courseId(), context.unitId(), false);
    JsonObject resultBody;
    if(!ajEntityUnits.isEmpty()) {
      LOGGER.info("unit {} found, packing into JSON", context.unitId());
      resultBody = new AJResponseJsonTransformer().transformUnit(ajEntityUnits.get(0).toJson(false, AJEntityUnit.ALL_FIELDS));
      
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
