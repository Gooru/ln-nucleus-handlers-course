package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public interface DBHandler {
        ExecutionResult<MessageResponse> checkSanity();

    ExecutionResult<MessageResponse> validateRequest();

    ExecutionResult<MessageResponse> executeRequest();

    boolean handlerReadOnly();
}
