package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth;

import io.vertx.core.json.JsonArray;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.TenantTree;
import org.gooru.nucleus.libs.tenant.TenantTreeBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorization;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorizationBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTreeAttributes;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 20/1/17.
 */
class TenantCollaboratorAuthorizer implements Authorizer<AJEntityCourse> {

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private final JsonArray collaborators;
  private static final Logger LOGGER = LoggerFactory.getLogger(TenantCollaboratorAuthorizer.class);

  public TenantCollaboratorAuthorizer(ProcessorContext context, JsonArray collaborators) {
    this.context = context;
    this.collaborators = collaborators;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityCourse model) {
    if (collaborators == null || collaborators.isEmpty()) {
      return sendSuccess();
    }
    TenantTree contentTenantTree = TenantTreeBuilder
        .build(model.getTenant(), model.getTenantRoot());
    List<TenantTree> collaboratorTenantTrees = getUserTenantTreesForCollaborators();
    if (collaboratorTenantTrees.size() != collaborators.size()) {
      LOGGER.warn("Not all collaborators are present in DB");
      return sendError();
    }
    return authorizeCollaborators(contentTenantTree, collaboratorTenantTrees);
  }

  private ExecutionResult<MessageResponse> authorizeCollaborators(TenantTree contentTenantTree,
      List<TenantTree> collaboratorTenantTrees) {
    ContentTreeAttributes attributes = ContentTreeAttributes.build(false);
    for (TenantTree collaboratorTree : collaboratorTenantTrees) {
      ContentTenantAuthorization authorization =
          ContentTenantAuthorizationBuilder.build(contentTenantTree, collaboratorTree, attributes);
      if (authorization.canCollaborate()) {
        continue;
      } else {
        return sendError();
      }
    }
    return sendSuccess();
  }

  private List<TenantTree> getUserTenantTreesForCollaborators() {
    LazyList<AJEntityUser> collaboratorsListFromDB = AJEntityUser
        .getCollaboratorsTenantInfo(this.collaborators);
    List<TenantTree> result = new LinkedList<>();
    for (AJEntityUser collaborator : collaboratorsListFromDB) {
      TenantTree tenantTree = TenantTreeBuilder
          .build(collaborator.getTenant(), collaborator.getTenantRoot());
      result.add(tenantTree);
    }
    return result;
  }

  private ExecutionResult<MessageResponse> sendError() {
    return new ExecutionResult<>(
        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
        ExecutionResult.ExecutionStatus.FAILED);
  }

  private ExecutionResult<MessageResponse> sendSuccess() {
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }
}
