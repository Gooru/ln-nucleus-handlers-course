package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.exceptionhandlers;

import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
/**
 * @author renuka
 * 
 */
class DefaultExceptionHandler implements ExceptionHandler {

  private DefaultExceptionHandler() {}

  public static DefaultExceptionHandler getInstance() {
    return new DefaultExceptionHandler();
  }

  @Override
  public ExecutionResult<MessageResponse> handleError(Throwable e) {
    // Most probably we do not know what to do with this, so send internal error
    return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()),
        ExecutionResult.ExecutionStatus.FAILED);
  }

}
