package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.create;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.LessonPlanDao;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

public final class LessonPlanCreateCommand {

  private ProcessorContext context;
  private AJEntityCourse course;

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLessonPlanHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  LessonPlanCreateCommand(ProcessorContext context) {
    this.context = context;
  }

  static LessonPlanCreateCommand build(ProcessorContext context) {
    LessonPlanCreateCommand lessonPlanCreateCommand = new LessonPlanCreateCommand(context);
    lessonPlanCreateCommand.validateRequest();
    return lessonPlanCreateCommand;
  }

  private void validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse =
        AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, context.courseId(),
            false, context.userId(), context.userId());
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("user is not owner or collaborator of course to create lesson plan. aborting");
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

    if (LessonPlanDao.checkLessonPlanExists(context.courseId(), context.unitId(),
        context.lessonId())) {
      LOGGER.warn("Lesson Plan already exists with this {} lesson, aborting", context.lessonId());
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("lesson.plan.already.exists")));
    }
  }


  public AJEntityCourse getCourse() {
    return this.course;
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
