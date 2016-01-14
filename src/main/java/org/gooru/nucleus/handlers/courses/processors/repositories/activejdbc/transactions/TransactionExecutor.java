package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions;

import org.gooru.nucleus.handlers.courses.app.components.DataSourceRegistry;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by ashish on 11/1/16.
 */
public class TransactionExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExecutor.class);

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
        Base.commitTransaction();
      }
      return executionResult;
    } catch (Throwable e) {
      Base.rollbackTransaction();
      LOGGER.error("Caught exeption, need to rollback and abort", e);
      // Most probably we do not know what to do with this, so send internal error
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()),
        ExecutionResult.ExecutionStatus.FAILED);
    } finally {
      if (handler.handlerReadOnly()) {
        // restore the settings
        try {
          Base.connection().setReadOnly(false);
        } catch (SQLException e) {
          LOGGER.error("Exception while marking connetion to be read/write", e);
        }
      }
      Base.close();
    }
  }
}
