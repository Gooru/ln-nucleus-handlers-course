package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.TenantTree;
import org.gooru.nucleus.libs.tenant.TenantTreeBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorization;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorizationBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTreeAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 16/1/17.
 */
class TenantAuthorizer implements Authorizer<AJEntityCourse> {

  private final ProcessorContext context;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final Logger LOGGER = LoggerFactory.getLogger(TenantAuthorizer.class);

  public TenantAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityCourse model) {
    TenantTree userTenantTree = TenantTreeBuilder.build(context.tenant(), context.tenantRoot());
    TenantTree contentTenantTree = TenantTreeBuilder
        .build(model.getString(AJEntityCourse.TENANT), model.getString(AJEntityCourse.TENANT_ROOT));

    ContentTenantAuthorization authorization = ContentTenantAuthorizationBuilder
        .build(contentTenantTree, userTenantTree,
            ContentTreeAttributes.build(model.isCoursePublished()));

    if (authorization.canRead()) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
        ExecutionResult.ExecutionStatus.FAILED);
  }
}
