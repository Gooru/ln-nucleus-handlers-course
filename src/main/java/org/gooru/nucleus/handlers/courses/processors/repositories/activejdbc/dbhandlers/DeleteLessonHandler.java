package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteLessonHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteLessonHandler.class);

  public DeleteLessonHandler(ProcessorContext context) {
    this.context = context;
  }
  
  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean handlerReadOnly() {
    // TODO Auto-generated method stub
    return false;
  }

}
