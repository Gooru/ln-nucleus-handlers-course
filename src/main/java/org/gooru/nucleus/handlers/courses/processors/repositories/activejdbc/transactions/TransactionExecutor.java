package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions;

import java.sql.SQLException;
import java.util.List;

import org.gooru.nucleus.handlers.courses.app.components.DataSourceRegistry;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.exceptionhandlers.ExceptionHandler;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.exceptionhandlers.ExceptionHandlerRegistry;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 11/1/16.
 */
public class TransactionExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExecutor.class);
  private static final List<ExceptionHandler> EXCEPTION_HANDLERS = ExceptionHandlerRegistry.getInstance().getHandlers() ;

  public MessageResponse executeTransaction(DBHandler handler) {
    // First validations without any DB
    ExecutionResult<MessageResponse> executionResult = handler.checkSanity();
    // Now we need to run with transaction, if we are going to continue
    if (executionResult.continueProcessing()) {
      executionResult = executeWithTransaction(handler);
    }
    return executionResult.result();

  }

  private ExecutionResult<MessageResponse> executeWithTransaction(DBHandler handler) {
    ExecutionResult<MessageResponse> executionResult;

    try {
      Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
      // If we need a read only transaction, then it is time to set up now
      if (handler.handlerReadOnly()) {
        Base.connection().setReadOnly(true);
      }
      Base.openTransaction();
      executionResult = handler.validateRequest();
      if (executionResult.continueProcessing()) {
        executionResult = handler.executeRequest();
        if (executionResult.isSuccessful()) {
          Base.commitTransaction();
        } else {
          Base.rollbackTransaction();
        }
      } else {
        Base.rollbackTransaction();
      }
      return executionResult;
    } catch (Throwable e) {
      Base.rollbackTransaction();
      LOGGER.error("Caught exception, need to rollback and abort", e);
      ExecutionResult<MessageResponse> executionResponse = null;
      for (ExceptionHandler exceptionHandler : EXCEPTION_HANDLERS) {
        executionResponse = exceptionHandler.handleError(e);
        if (executionResponse.result() != null) {
          break;
        }
      }
      return executionResponse;
    } finally {
      if (handler.handlerReadOnly()) {
        // restore the settings
        try {
          Base.connection().setReadOnly(false);
        } catch (SQLException e) {
          LOGGER.error("Exception while marking connection to be read/write", e);
        }
      }
      Base.close();
    }
  }
}
