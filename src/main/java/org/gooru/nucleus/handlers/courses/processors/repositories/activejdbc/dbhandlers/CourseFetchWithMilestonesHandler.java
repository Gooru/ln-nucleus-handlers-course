package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common.RequestQueryParamReader;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.common.Validators;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityMilestone;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.MilestoneDao;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
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

class CourseFetchWithMilestonesHandler implements DBHandler {

  private static final String MILESTONES = "milestones";
  private final ProcessorContext context;
  private AJEntityCourse course;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CourseFetchWithMilestonesHandler.class);
  private UUID classId;
  private UUID userIdForRescope;
  private boolean needRescopedMilestoneLookup;

  CourseFetchWithMilestonesHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {

    try {
      Validators.validateCourseInContext(context);
      Validators.validateUserIdInContext(context);
      Validators.validateFWInContext(context);
      initializeRescopedMilestoneLookup();
      LOGGER.debug("checkSanity() OK");
      return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LOGGER.debug("validateRequest() OK");
    LazyList<AJEntityCourse> entityCourses =
        AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE, context.courseId(), false);
    if (!entityCourses.isEmpty()) {
      LOGGER.debug("Found Course: '{}'", context.courseId());
      course = entityCourses.get(0);
      if (course.isPremium()) {
        return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(course);
      } else {
        {
          LOGGER.warn("Course: '{}' is not premium", context.courseId());
          return new ExecutionResult<>(MessageResponseFactory
              .createInvalidRequestResponse(
                  "Milestone API is available for navigator courses only"),
              ExecutionStatus.FAILED);
        }
      }
    } else {
      LOGGER.error("Course not found {}", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject body = new JsonObject(
        new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityCourse.ALL_FIELDS)
            .toJson(course));
    LazyList<AJEntityMilestone> milestones;
    if (needRescopedMilestoneLookup) {
      milestones = fetchMilestonesForCourseConsideringRescopedLessons();
    } else {
      milestones = fetchMilestonesForCourseWithoutRescope();
    }
    LOGGER.debug("Number of milestones found {}", milestones.size());
    JsonArray milestonesSummary;
    if (milestones.size() > 0) {
      milestonesSummary = new JsonArray(new JsonFormatterBuilder()
          .buildSimpleJsonFormatter(false, MilestoneDao.MILESTONE_SUMMARY_FIELDS)
          .toJson(milestones));
    } else {
      milestonesSummary = new JsonArray();
    }
    body.put(MILESTONES, milestonesSummary);
    return new ExecutionResult<>(MessageResponseFactory.createGetResponse(body),
        ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

  private LazyList<AJEntityMilestone> fetchMilestonesForCourseConsideringRescopedLessons() {
    LOGGER.debug("Fetching course milestones with rescope. Course: '{}'");
    return new MilestoneDao()
        .fetchMilestonesForCourseConsideringRescope(userIdForRescope.toString(),
            context.courseId(), classId.toString(), context.frameworkCode());
  }

  private LazyList<AJEntityMilestone> fetchMilestonesForCourseWithoutRescope() {
    LOGGER.debug("Fetching course milestones without rescope. Course: '{}'");
    return new MilestoneDao()
        .fetchMilestonesForCourse(context.courseId(), context.frameworkCode());
  }

  private void initializeRescopedMilestoneLookup() {
    classId = RequestQueryParamReader.readQueryParamAsUuid(context.request(), "class_id");
    userIdForRescope = RequestQueryParamReader.readQueryParamAsUuid(context.request(), "user_id");
    if ((classId != null && userIdForRescope == null) || (classId == null
        && userIdForRescope != null)) {
      LOGGER
          .warn("Both class_id and user_id should be provided or both should absent. Course: '{}'",
              context.courseId());
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          "Both class_id and user_id should be provided or both should absent"));
    }
    needRescopedMilestoneLookup = (classId != null && userIdForRescope != null);
  }


}
