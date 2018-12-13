package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth;

import io.vertx.core.json.JsonArray;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;

/**
 * Created by ashish on 16/01/17.
 */
public final class AuthorizerBuilder {

  private AuthorizerBuilder() {
    throw new AssertionError();
  }

  public static Authorizer<AJEntityCourse> buildTenantAuthorizer(ProcessorContext context) {
    return new TenantAuthorizer(context);
  }

  public static Authorizer<AJEntityCourse> buildTenantCollaboratorAuthorizer(
      ProcessorContext context,
      JsonArray collaborators) {
    return new TenantCollaboratorAuthorizer(context, collaborators);
  }
}
