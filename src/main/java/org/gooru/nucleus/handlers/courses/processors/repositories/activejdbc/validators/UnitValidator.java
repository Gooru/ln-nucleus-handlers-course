package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;

/**
 * 
 * @author Sachin
 *
 */
public class UnitValidator {

  private final ProcessorContext context;
  private final Logger LOGGER;
  
  public UnitValidator(ProcessorContext context, Logger logger) {
    this.context = context;
    this.LOGGER = logger;
  }
  
  public ExecutionResult<MessageResponse> checkIsDeleted() {
    String sql = "SELECT " + AJEntityUnit.IS_DELETED + " FROM course_unit WHERE " + AJEntityUnit.UNIT_ID + " = ?";
    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(sql, context.unitId());

    if (!ajEntityUnit.isEmpty()) {
      if (ajEntityUnit.size() >= 2) {
        // only log, if more than one course is found
        LOGGER.debug("more that 1 unit found for id {}", context.unitId());
      }

      // irrespective of size, always get first
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
  
  public ExecutionResult<MessageResponse> checkIsDeletedAndOwner() {
    return null;
  }

}
