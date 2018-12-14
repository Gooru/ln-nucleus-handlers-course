package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.javalite.activejdbc.Model;

/**
 * Created by ashish on 16/01/17.
 */
public interface Authorizer<T extends Model> {

  ExecutionResult<MessageResponse> authorize(T model);

}
