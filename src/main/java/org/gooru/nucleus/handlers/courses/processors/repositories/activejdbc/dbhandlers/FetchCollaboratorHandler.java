package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.AJResponseJsonTransformer;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.CourseEntityConstants;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class FetchCollaboratorHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCollaboratorHandler.class);

  public FetchCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id to fetch collaborator");
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id to fetch collaborator"),
              ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    if (!AJEntityCourse.exists(context.courseId())) {
      LOGGER.info("course {} not found to fetch collaborator, aborting", context.courseId());
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    String courseId = context.courseId();
    String sql = "SELECT " + CourseEntityConstants.COLLABORATOR + " FROM COURSE WHERE " + CourseEntityConstants.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, courseId);
    JsonObject body = new JsonObject();
    if (ajEntityCourse != null && !ajEntityCourse.isEmpty()) {
      LOGGER.info("collaborator found for course {}", courseId);
      body = new AJResponseJsonTransformer().transform(ajEntityCourse.get(0).toJson(false, CourseEntityConstants.COLLABORATOR));
    } else {
      LOGGER.info("no collaborator found for course {}", courseId);
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<MessageResponse>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
