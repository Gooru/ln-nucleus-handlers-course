package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gooru.nucleus.handlers.courses.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.create.LessonPlanCreateCommand;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.delete.LessonPlanDeleteCommand;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.lessonplan.update.LessonPlanUpdateCommand;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter.ModelErrorFormatter;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class LessonPlanDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(LessonPlanDao.class);


  private LessonPlanDao() {
    throw new AssertionError();
  }

  private static final Set<String> EDITABLE_FIELDS = new HashSet<>(
      Arrays.asList(AJEntityLessonPlan.DESCRIPTION, AJEntityLessonPlan.GUIDING_QUESTIONS,
          AJEntityLessonPlan.PRIOR_KNOWLEDGE, AJEntityLessonPlan.ANTICIPATED_STRUGGLES,
          AJEntityLessonPlan.REFERENCE_LINKS, AJEntityLessonPlan.PACING_GUIDE_IN_DAYS,
          AJEntityLessonPlan.START_WEEK, AJEntityLessonPlan.SESSIONS));

  private static final Set<String> CREATABLE_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.DESCRIPTION,
          AJEntityLessonPlan.GUIDING_QUESTIONS, AJEntityLessonPlan.PRIOR_KNOWLEDGE,
          AJEntityLessonPlan.ANTICIPATED_STRUGGLES, AJEntityLessonPlan.REFERENCE_LINKS,
          AJEntityLessonPlan.PACING_GUIDE_IN_DAYS, AJEntityLessonPlan.START_WEEK));

  private static final Set<String> MANDATORY_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.DESCRIPTION));

  private static final Set<String> CREATABLE_SESSION_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.TITLE, AJEntityLessonPlan.DESCRIPTION,
          AJEntityLessonPlan.DURATION, AJEntityLessonPlan.CONTENTS));

  private static final Set<String> CREATABLE_SESSION_CONTENT_FIELDS = new HashSet<>(
      Arrays.asList(AJEntityLessonPlan.CONTENT_ID, AJEntityLessonPlan.CONTENT_FORMAT));

  private static final Set<String> MANDATORY_SESSION_FIELDS =
      new HashSet<>(Arrays.asList(AJEntityLessonPlan.TITLE));

  private static final Set<String> MANDATORY_SESSION_CONTENT_FIELDS = new HashSet<>(
      Arrays.asList(AJEntityLessonPlan.CONTENT_ID, AJEntityLessonPlan.CONTENT_FORMAT));


  public static final String SELECT_LESSON_PLAN_TO_VALIDATE =
      "SELECT id, lesson_id, unit_id, course_id FROM lesson_plan WHERE id = ?::bigint AND lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid";


  public static final List<String> FETCH_FIELD_LIST =
      Arrays.asList("id", "description", "login_required", "course_id", "unit_id", "lesson_id");

  public static FieldSelector editFieldSelector() {
    return () -> Collections.unmodifiableSet(EDITABLE_FIELDS);
  }

  public static FieldSelector createSessionContentFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_SESSION_CONTENT_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_SESSION_CONTENT_FIELDS);
      }
    };
  }

  public static FieldSelector createSessionFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_SESSION_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_SESSION_FIELDS);
      }
    };
  }

  public static FieldSelector createFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_FIELDS);
      }
    };
  }

  public static ValidatorRegistry getValidatorRegistry() {
    return new LessonPlanValidationRegistry();
  }

  public static ValidatorRegistry getSessionValidatorRegistry() {
    return new LessonPlanSessionValidationRegistry();
  }

  public static ValidatorRegistry getSessionContentValidatorRegistry() {
    return new LessonPlanSessionContentValidationRegistry();
  }

  public static ConverterRegistry getConverterRegistry() {
    return new LessonPlanConverterRegistry();
  }

  private static class LessonPlanValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorRegistry.get(fieldName);
    }
  }

  private static class LessonPlanSessionValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorSessionRegistry.get(fieldName);
    }
  }

  private static class LessonPlanSessionContentValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return AJEntityLessonPlan.validatorSessionContentRegistry.get(fieldName);
    }
  }


  private static class LessonPlanConverterRegistry implements ConverterRegistry {

    @Override
    public FieldConverter lookupConverter(String fieldName) {
      return AJEntityLessonPlan.converterRegistry.get(fieldName);
    }
  }

  public static String createLessonPlan(LessonPlanCreateCommand command) {
    AJEntityLessonPlan lessonPlan = new AJEntityLessonPlan();
    autoPopulateFields(command, lessonPlan);
    new EntityBuilder<AJEntityLessonPlan>() {}.build(lessonPlan, command.requestPayload(),
        LessonPlanDao.getConverterRegistry());
    boolean result = lessonPlan.save();
    if (!result) {
      LOGGER.error("Lesson Plan  creation failed for user '{}'", command.getUserId());
      if (lessonPlan.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(lessonPlan)));
      }
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInternalErrorResponse());
    }
    updateCourseUpdatedAt(command.getCourse());
    return lessonPlan.getId().toString();
  }

  public static void updateLessonPlan(LessonPlanUpdateCommand command) {
    AJEntityLessonPlan lessonPlan = command.getLessonPlan();
    new EntityBuilder<AJEntityLessonPlan>() {}.build(lessonPlan, command.requestPayload(),
        LessonPlanDao.getConverterRegistry());
    boolean result = lessonPlan.save();
    if (!result) {
      LOGGER.error("Lesson Plan  updation failed for user '{}'", command.getUserId());
      if (lessonPlan.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(lessonPlan)));
      }
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInternalErrorResponse());
    }
  }

  public static void deleteLessonPlan(LessonPlanDeleteCommand command) {
    AJEntityLessonPlan lessonPlan = command.getLessonPlan();
    boolean result = lessonPlan.delete();
    if (!result) {
      LOGGER.error("Lesson Plan  deletion failed for user '{}'", command.getUserId());
      if (lessonPlan.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(lessonPlan)));
      }
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInternalErrorResponse());
    }
  }



  private static void updateCourseUpdatedAt(AJEntityCourse course) {
    course.setTimestamp(AJEntityCourse.UPDATED_AT, new Timestamp(System.currentTimeMillis()));
    boolean result = course.save();
    if (!result) {
      LOGGER.error("Course with id '{}' failed to save modified time stamp", course.getId());
      if (course.hasErrors()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createValidationErrorResponse(ModelErrorFormatter.formattedError(course)));
      }
    }
  }

  private static void autoPopulateFields(LessonPlanCreateCommand command,
      AJEntityLessonPlan lessonPlan) {
    lessonPlan.setCourseId(command.getCourseId());
    lessonPlan.setUnitId(command.getUnitId());
    lessonPlan.setLessonId(command.getLessonId());
  }

}
