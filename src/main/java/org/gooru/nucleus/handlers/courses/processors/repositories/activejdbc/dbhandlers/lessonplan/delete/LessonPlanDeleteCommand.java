package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.delete;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLessonPlan;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.LessonPlanDao;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

public final class LessonPlanDeleteCommand {

  private ProcessorContext context;
  private AJEntityCourse course;
  private AJEntityLessonPlan lessonPlan;

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteLessonPlanHandler.class);

  LessonPlanDeleteCommand(ProcessorContext context) {
    this.context = context;
  }

  static LessonPlanDeleteCommand build(ProcessorContext context) {
    LessonPlanDeleteCommand lessonPlanDeleteCommand = new LessonPlanDeleteCommand(context);
    lessonPlanDeleteCommand.validateRequest();
    return lessonPlanDeleteCommand;
  }

  private void validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse =
        AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, context.courseId(),
            false, context.userId(), context.userId());
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of course to delete lesson plan. aborting");
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

    LazyList<AJEntityLessonPlan> ajEntityLessonPlan = AJEntityLesson.findBySQL(
        LessonPlanDao.SELECT_LESSON_PLAN_TO_VALIDATE, context.lessonPlanId(), context.lessonId(),
        context.unitId(), context.courseId(), false);
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

}
