package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.update;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLessonPlan;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.LessonPlanDao;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class LessonPlanUpdateCommand {

  private ProcessorContext context;
  private AJEntityCourse course;
  private AJEntityLessonPlan lessonPlan;

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLessonPlanHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  LessonPlanUpdateCommand(ProcessorContext context) {
    this.context = context;
  }

  static LessonPlanUpdateCommand build(ProcessorContext context) {
    LessonPlanUpdateCommand lessonPlanUpdateCommand = new LessonPlanUpdateCommand(context);
    lessonPlanUpdateCommand.validate();
    return lessonPlanUpdateCommand;
  }

  private void validate() {
    JsonArray sessionsData = requestPayload().getJsonArray(AJEntityLessonPlan.SESSIONS, null);
    if (requestPayload().containsKey(AJEntityLessonPlan.SESSIONS) && (sessionsData == null || sessionsData.isEmpty())) { 
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("lesson.plan.session.empty")));
    }
    if (sessionsData != null) {
      JsonArray sessionErrorList = new JsonArray();
      sessionsData.forEach(session -> {
        JsonObject sessionData = (JsonObject) session;
        JsonObject errors = new SessionPayloadValidator().validatePayload(sessionData,
            LessonPlanDao.createSessionFieldSelector(),
            LessonPlanDao.getSessionValidatorRegistry());
        JsonArray contentErrorList = new JsonArray();
        JsonArray contents = sessionData.getJsonArray(AJEntityLessonPlan.CONTENTS, null);
        if (contents != null && !contents.isEmpty()) {
          contents.forEach(content -> {
            JsonObject contentData = (JsonObject) content;
            JsonObject contentErrors = new SessionContentPayloadValidator().validatePayload(
                contentData, LessonPlanDao.createSessionContentFieldSelector(),
                LessonPlanDao.getSessionContentValidatorRegistry());
            if (contentErrors != null) {
              contentErrorList.add(contentErrors);
            }
          });
          if (!contentErrorList.isEmpty()) {
            errors = errors != null ? errors : new JsonObject();
            errors.put(AJEntityLessonPlan.CONTENTS, contentErrorList);
          }
        }
        if (errors != null) {
          sessionErrorList.add(errors);
        }
      });
      if (!sessionErrorList.isEmpty()) {
        LOGGER.warn("Validation errors for request");
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(new JsonObject().put("errors", sessionErrorList)));
      }
    }
  }


  protected void validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse =
        AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, context.courseId(),
            false, context.userId(), context.userId());
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of course to update lesson plan. aborting");
      throw new MessageResponseWrapperException(MessageResponseFactory.createForbiddenResponse());
    }
    this.course = ajEntityCourse.get(0);

    LazyList<AJEntityUnit> ajEntityUnit = AJEntityUnit.findBySQL(
        AJEntityUnit.SELECT_UNIT_TO_VALIDATE, context.unitId(), context.courseId(), false);
    if (ajEntityUnit.isEmpty()) {
      LOGGER.warn("Unit {} not found, aborting", context.unitId());
      throw new MessageResponseWrapperException(MessageResponseFactory.createNotFoundResponse());
    }

    LazyList<AJEntityLesson> ajEntityLesson =
        AJEntityLesson.findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, context.lessonId(),
            context.unitId(), context.courseId(), false);
    if (ajEntityLesson.isEmpty()) {
      LOGGER.warn("Lesson {} not found, aborting", context.lessonId());
      throw new MessageResponseWrapperException(MessageResponseFactory.createNotFoundResponse());
    }

    LazyList<AJEntityLessonPlan> ajEntityLessonPlan = AJEntityLessonPlan.findBySQL(
        LessonPlanDao.SELECT_LESSON_PLAN_TO_VALIDATE, context.lessonPlanId(), context.lessonId(),
        context.unitId(), context.courseId());
    if (ajEntityLessonPlan.isEmpty()) {
      LOGGER.warn("Lesson Plan {} not found, aborting", context.lessonPlanId());
      throw new MessageResponseWrapperException(MessageResponseFactory.createNotFoundResponse());
    }
    this.lessonPlan = ajEntityLessonPlan.get(0);
  }

  public AJEntityCourse getCourse() {
    return this.course;
  }

  public AJEntityLessonPlan getLessonPlan() {
    return this.lessonPlan;
  }

  public JsonObject requestPayload() {
    return this.context.request();
  }

  public String getCourseId() {
    return this.context.courseId();
  }

  public String getUnitId() {
    return this.context.unitId();
  }

  public String getLessonId() {
    return this.context.lessonId();
  }

  public String getUserId() {
    return this.context.userId();
  }

  private static class SessionPayloadValidator implements PayloadValidator {

  }

  private static class SessionContentPayloadValidator implements PayloadValidator {

  }


}
