package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.AJResponseJsonTransformer;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class FetchCollaboratorHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCollaboratorHandler.class);
  private final ProcessorContext context;

  public FetchCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id to fetch collaborator");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id to fetch collaborator"),
        ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    String sql = "SELECT " + AJEntityCourse.IS_DELETED + " FROM course WHERE " + AJEntityCourse.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());
    
    if (!ajEntityCourse.isEmpty()) {
      //irrespective of size, always get first 
      if (ajEntityCourse.get(0).getBoolean(AJEntityCourse.IS_DELETED)) {
        LOGGER.info("course {} is deleted. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course is deleted for which your are trying to fetch collaborators."),
          ExecutionStatus.FAILED);
      }

    } else {
      LOGGER.info("course {} not found to delete, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    String courseId = context.courseId();
    String sql = "SELECT " + AJEntityCourse.COLLABORATOR + " FROM COURSE WHERE " + AJEntityCourse.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, courseId);
    JsonObject body;
    if (ajEntityCourse != null && !ajEntityCourse.isEmpty()) {
      LOGGER.info("collaborator found for course {}", courseId);
      body = new AJResponseJsonTransformer().transformCourse(ajEntityCourse.get(0).toJson(false, AJEntityCourse.COLLABORATOR));
    } else {
      LOGGER.info("no collaborator found for course {}", courseId);
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
