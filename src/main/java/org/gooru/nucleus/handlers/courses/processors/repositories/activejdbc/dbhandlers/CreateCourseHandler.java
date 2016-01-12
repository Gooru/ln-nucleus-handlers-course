package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.postgresql.util.PGobject;

import io.vertx.core.json.JsonObject;

public class CreateCourseHandler implements DBHandler {

  private final ProcessorContext context;

  public CreateCourseHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.request() == null || context.request().isEmpty()) {
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInvalidRequestResponse("Invalid data provided request"),
              ExecutionStatus.FAILED);
    }

    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    JsonObject request = context.request();
    /*
     * Validations to create course 1. course title is not null
     */
    for (String fieldName : CourseRepo.NOTNULL_FIELDS) {
      if (request.getString(fieldName) == null || request.getString(fieldName).isEmpty()) {
        return new ExecutionResult<MessageResponse>(
                MessageResponseFactory.createInvalidRequestResponse("mandatory field '" + fieldName + "' is missing"), ExecutionStatus.FAILED);
      }
    }

    return new ExecutionResult<MessageResponse>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject request = context.request();
    AJEntityCourse course = new AJEntityCourse();
    String mapValue = null;
    try {
      for (Map.Entry<String, Object> entry : request) {
        mapValue = (entry.getValue() != null) ? entry.getValue().toString() : null;
        if(mapValue != null && !mapValue.isEmpty()) {
          if (Arrays.asList(CourseRepo.JSON_FIELDS).contains(entry.getKey())) {
            PGobject jsonbField = new PGobject();
            jsonbField.setType("jsonb");
            jsonbField.setValue(mapValue);
            course.set(entry.getKey(), jsonbField);
          } else {
            course.set(entry.getKey(), entry.getValue());
          }
        }
      }
      
      //generate UUID and set as id
      String id = UUID.randomUUID().toString();
      course.setId(id);
      //creator_id character varying(36) NOT NULL,
      course.set(CourseRepo.CREATOR_ID, context.userId());
      //original_creator_id character varying(36) NOT NULL,
      course.set(CourseRepo.ORIGINAL_CREATOR_ID, context.userId());
      
      if(course.insert()) {
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createPostResponse(id), ExecutionStatus.SUCCESSFUL);
      } else {
        return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse("Not able to save course. try again!"), ExecutionStatus.FAILED);
      }
    } catch (SQLException sqle) {
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse(sqle.getMessage()), ExecutionStatus.FAILED);
    } catch (Exception e) {
      return new ExecutionResult<MessageResponse>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionStatus.FAILED);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
