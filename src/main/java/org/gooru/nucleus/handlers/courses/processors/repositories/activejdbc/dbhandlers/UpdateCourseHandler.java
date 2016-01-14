package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.CourseEntityConstants;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class UpdateCourseHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCourseHandler.class);
  private final ProcessorContext context;

  public UpdateCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.courseId() == null || context.courseId().isEmpty()) {
      LOGGER.info("invalid course id for update");
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for update"),
        ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.info("invalid data provided to update course {}", context.courseId());
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to update course"),
        ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    //Check if course is exists or not
    if (!AJEntityCourse.exists(context.courseId())) {
      LOGGER.info("course {} not found to update, aborting", context.courseId());
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    // Check whether the course is not deleted
    String sql = "SELECT is_deleted FROM course WHERE id = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());
    if (ajEntityCourse != null && !ajEntityCourse.isEmpty()) {
      if (ajEntityCourse.get(0).getBoolean(CourseEntityConstants.IS_DELETED)) {
        LOGGER.info("course {} is deleted, hence can't be updated. Aborting", context.courseId());
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createNotFoundResponse("Course your are trying to update is deleted"),
          ExecutionStatus.FAILED);
      }
    }

    //TODO: check whether user is owner/collaborator of course
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject request = context.request();
    AJEntityCourse course = new AJEntityCourse();
    String mapValue = null;
    try {
      //TODO: Define list of updatable columns and only set those values
      for (Map.Entry<String, Object> entry : request) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (Arrays.asList(CourseEntityConstants.JSON_FIELDS).contains(entry.getKey())) {
            PGobject jsonbField = new PGobject();
            jsonbField.setType("jsonb");
            jsonbField.setValue(mapValue);
            course.set(entry.getKey(), jsonbField);
          } else {
            course.set(entry.getKey(), entry.getValue());
          }
        }
      }
      //May be this is not required if id is present in incoming request data
      course.setId(context.courseId());

      if (course.save()) {
        LOGGER.info("course {} updated successfully", context.courseId());
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createPutResponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.info("error in updating course");
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createValidationErrorResponse(course.errors()), ExecutionStatus.FAILED);
      }
    } catch (Throwable t) {
      LOGGER.error("Exception while updating course", t);
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
