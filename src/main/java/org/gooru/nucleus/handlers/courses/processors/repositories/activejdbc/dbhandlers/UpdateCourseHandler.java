package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public class UpdateCourseHandler implements DBHandler {

  private final ProcessorContext context;
  
  public UpdateCourseHandler(ProcessorContext context) {
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
    return false;
  }

}
