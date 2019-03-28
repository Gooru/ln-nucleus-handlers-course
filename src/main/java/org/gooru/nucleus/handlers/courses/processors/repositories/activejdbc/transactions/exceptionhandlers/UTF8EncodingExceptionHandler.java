package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.exceptionhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.DBException;
import org.postgresql.util.PSQLException;
/**
 * @author renuka
 * 
 */
class UTF8EncodingExceptionHandler implements ExceptionHandler {

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private UTF8EncodingExceptionHandler() {}

  public static UTF8EncodingExceptionHandler getInstance() {
    return new UTF8EncodingExceptionHandler();
  }

  @Override
  public ExecutionResult<MessageResponse> handleError(Throwable e) {
    if (e instanceof DBException && e.getCause() instanceof PSQLException
        && ((PSQLException) e.getCause()).getSQLState().equals("22021")) {
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.textfield")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.FAILED);
  }
}
