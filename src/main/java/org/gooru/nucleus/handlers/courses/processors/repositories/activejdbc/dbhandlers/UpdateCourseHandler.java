package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.util.Arrays;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.AJValidatorBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

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
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid course id for update"),
        ExecutionStatus.FAILED);
    }

    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.info("invalid data provided to update course {}", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to update course"),
        ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    return new AJValidatorBuilder().buildCourseValidator(context, LOGGER).checkIsDeletedAndOwner();

    /*//Check whether the course is deleted or not, which will also verify if course exists or not
    String sql = "SELECT " + CourseEntityConstants.IS_DELETED + ", " + CourseEntityConstants.CREATOR_ID + " FROM course WHERE " + CourseEntityConstants.ID + " = ?";
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(sql, context.courseId());

    if (!ajEntityCourse.isEmpty()) {
      if(ajEntityCourse.size() >= 2) {
        //only log, if more than one course is found 
        LOGGER.debug("more that 1 course found for id {}", context.courseId());
      }
      
      //irrespective of size, always get first 
      if (ajEntityCourse.get(0).getBoolean(CourseEntityConstants.IS_DELETED)) {
        LOGGER.info("course {} is deleted, hence can't be updated. Aborting", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Course your are trying to update is deleted"),
          ExecutionStatus.FAILED);
      }
      
      //check whether user is owner, if anonymous or not owner, send unauthorized back;
      if(!ajEntityCourse.get(0).getString(CourseEntityConstants.CREATOR_ID).equalsIgnoreCase(context.userId())) {
        LOGGER.info("user is anonymous or not owner of course for delete. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
      }
    } else {
      LOGGER.info("course {} not found to update, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
    }

    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);*/
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject request = context.request();
    AJEntityCourse course = new AJEntityCourse();
    String mapValue;
    try {
      //TODO: Define list of updatable columns and only set those values
      for (Map.Entry<String, Object> entry : request) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if (mapValue != null && !mapValue.isEmpty()) {
          if (Arrays.asList(AJEntityCourse.JSON_FIELDS).contains(entry.getKey())) {
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
      course.set(AJEntityCourse.MODIFIER_ID, context.userId());
      
      if (course.save()) {
        LOGGER.info("course {} updated successfully", context.courseId());
        return new ExecutionResult<>(MessageResponseFactory.createPutResponse(context.courseId()), ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.info("error in updating course");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(course.errors()), ExecutionStatus.FAILED);
      }
    } catch (Throwable t) {
      LOGGER.error("Exception while updating course", t);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(t.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
