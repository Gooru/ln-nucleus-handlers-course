package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author subbu Created On: 16-Oct-2018
 */
public class FetchCourseCardsHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCourseCardsHandler.class);

  private enum ResponseType {
    CARD, SUMMARY, DETAIL;
  }

  private List<String> courseIds;
  private String searchValue;
  private ResponseType responseType = ResponseType.CARD;

  public FetchCourseCardsHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    JsonArray inputArray = context.request().getJsonArray("ids");
    searchValue = inputArray != null ? inputArray.getString(0) : null;

    if (searchValue == null || searchValue.isEmpty()) {
      LOGGER.warn("Invalid list of courses");
      return new ExecutionResult<>(
          MessageResponseFactory.createInvalidRequestResponse("Invalid course cards request"),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity: list of ids at request: " + searchValue);
    LOGGER.debug("checkSanity: OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    courseIds = Arrays.asList(searchValue.split(","));
    LOGGER.debug("validateRequest: courseIDs: " + courseIds);

    for (String courseId : courseIds) {
      try {
        UUID.fromString(courseId);
      } catch (IllegalArgumentException e) {
        LOGGER.error("validateRequest: Invalid course id: " + courseId, e);
        return new ExecutionResult<>(
            MessageResponseFactory
                .createInvalidRequestResponse("Invalid course id passed in the request"),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    LOGGER.debug("validateRequest: OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // if responseType == ResponseType.CARD  <<TBD: need to code for other response types
    LazyList<AJEntityCourse> courses =
        AJEntityCourse.findBySQL(AJEntityCourse.SELECT_FOR_CARD,
            DbHelperUtil.toPostgresArrayString(courseIds));
    if (courses.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    JsonArray resultArray = new JsonArray(new JsonFormatterBuilder()
        .buildSimpleJsonFormatter(false, AJEntityCourse.CARD_FIELDS)
        .toJson(courses));

    JsonObject result = new JsonObject();
    result.put(MessageConstants.RESP_JSON_KEY_COURSES, resultArray);

    return new ExecutionResult<>(MessageResponseFactory.createGetResponse(result),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }
}
