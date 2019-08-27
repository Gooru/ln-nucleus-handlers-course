package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.update;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common.Validators;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.LessonPlanDao;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateLessonPlanHandler implements DBHandler {

  private final ProcessorContext context;
  private LessonPlanUpdateCommand command;

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLessonPlanHandler.class);

  public UpdateLessonPlanHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      Validators.validateUser(context);
      Validators.validateCourseInContext(context);
      Validators.validateUnitInContext(context);
      Validators.validateLessonInContext(context);
      Validators.validatePayloadNotEmpty(context.request());
      Validators.validateWithDefaultPayloadValidator(context.request(),
          LessonPlanDao.editFieldSelector(), LessonPlanDao.getValidatorRegistry());
      command = LessonPlanUpdateCommand.build(context);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      command.validateRequest();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      LessonPlanDao.updateLessonPlan(command);
      return new ExecutionResult<>(
          MessageResponseFactory.createNoContentResponse(
              EventBuilderFactory.getUpdateLessonPlanEventBuilder(context.lessonPlanId())),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);

    } catch (MessageResponseWrapperException e) {
      return new ExecutionResult<>(e.getMessageResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

  }


  @Override
  public boolean handlerReadOnly() {
    return false;
  }


}
