package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common.Validators;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityMilestone;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.MilestoneDao;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class FetchMilestoneHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchMilestoneHandler.class);
  private AJEntityCourse course;
  private MilestoneDao milestoneDao;

  FetchMilestoneHandler(ProcessorContext context) {
    this.context = context;
  }


  @Override
  public ExecutionResult<MessageResponse> checkSanity() {

    try {
      Validators.validateCourseInContext(context);
      Validators.validateUserIdInContext(context);
      Validators.validateMilestoneInContext(context);
      LOGGER.debug("checkSanity() OK");
      return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      initializeCourse();
      LOGGER.debug("Found course: '{}", context.courseId());
      Validators.validateCourseIsPremium(course, context);
      LOGGER.debug("Course: '{}' is premium", context.courseId());
      validateAndInitializeMilestone();
      return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(course);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  private void validateAndInitializeMilestone() {
    milestoneDao = new MilestoneDao();
    if (!milestoneDao.checkMilestoneExists(context.milestoneId())) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createNotFoundResponse("Milestone does not exists"));
    }
  }

  private void initializeCourse() {
    LazyList<AJEntityCourse> entityCourses =
        AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE, context.courseId(), false);
    if (entityCourses.isEmpty()) {
      LOGGER.error("course not found {}", context.courseId());
      throw new MessageResponseWrapperException(MessageResponseFactory.createNotFoundResponse());
    }
    course = entityCourses.get(0);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonArray lessonsInMilestone = milestoneDao
        .fetchMilestoneBYIdForCourse(context.courseId(), context.milestoneId());
    LOGGER.debug("Found '{}' lessons for milestone '{}'", lessonsInMilestone.size(),
        context.milestoneId());
    JsonObject result = new JsonObject().put(MessageConstants.COURSE_ID, context.courseId())
        .put(MessageConstants.MILESTONE_ID, context.milestoneId())
        .put(AJEntityLesson.LESSONS, lessonsInMilestone);
    return new ExecutionResult<>(MessageResponseFactory.createGetResponse(result),
        ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }
}
