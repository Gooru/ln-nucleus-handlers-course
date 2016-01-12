package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.AJResponseJsonTransformer;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class FetchCourseHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCourseHandler.class);
  
  public FetchCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if(context.courseId() == null || context.courseId().isEmpty()) {
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id provided"), ExecutionStatus.FAILED);
    }
    
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    String courseId = context.courseId();
    AJEntityCourse ajEntityCourse = AJEntityCourse.findById(courseId);
    JsonObject body = new JsonObject();
    if (ajEntityCourse != null) {
      LOGGER.debug("course entity is not null");
      body = new AJResponseJsonTransformer()
              .transform(ajEntityCourse.toJson(false, CourseRepo.ALL_FIELDS));
      //TODO: Need to include unit summary of the course
    } else {
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }
    LOGGER.debug("returning body : " + body.toString());
    return new ExecutionResult<MessageResponse>(MessageResponseFactory.createGetResponse(body), ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
