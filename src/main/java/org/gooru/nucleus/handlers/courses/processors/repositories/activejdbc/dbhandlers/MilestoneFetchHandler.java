package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

class MilestoneFetchHandler implements DBHandler {

  private final ProcessorContext context;

  MilestoneFetchHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // TODO: Implement this
    return null;
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // TODO: Implement this
    return null;
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // TODO: Implement this
    return null;
  }

  @Override
  public boolean handlerReadOnly() {
    // TODO: Implement this
    return false;
  }
}
