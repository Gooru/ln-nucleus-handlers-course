package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldValidator;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table("lesson_plan")
@IdName("id")
public class AJEntityLessonPlan extends Model {

  private static final String COURSE_ID = "course_id";
  private static final String UNIT_ID = "unit_id";
  private static final String LESSON_ID = "lesson_id";
  public static final String DESCRIPTION = "description";
  public static final String TITLE = "title";
  public static final String DURATION = "duration";
  public static final String GUIDING_QUESTIONS = "guiding_questions";
  public static final String PRIOR_KNOWLEDGE = "prior_knowledge";
  public static final String ANTICIPATED_STRUGGLES = "anticipated_struggles";
  public static final String REFERENCE_LINKS = "reference_links";
  public static final String PACING_GUIDE_IN_HRS = "pacing_guide_in_hrs";
  public static final String START_WEEK = "start_week";
  public static final String SESSIONS = "sessions";
  public static final String TEACHER_CONTENTS = "teacher_contents";
  public static final String STUDENT_CONTENTS = "student_contents";
  public static final String CONTENT_FORMAT = "content_format";
  public static final String CONTENT_ID = "content_id";
  public static final String LESSON_PLAN_ID = "id";
  private static final Set<String> ALLOWED_TEACHER_CONTENT_FORMATS =
      new HashSet<>(Arrays.asList("assessment", "collection", "assessment-external",
          "collection-external", "offline-activity", "question", "resource"));
  private static final Set<String> ALLOWED_STUDENT_CONTENT_FORMATS =
      new HashSet<>(Arrays.asList("assessment", "collection", "assessment-external",
          "collection-external", "offline-activity"));

  static final Map<String, FieldValidator> validatorRegistry;
  static final Map<String, FieldValidator> validatorSessionRegistry;
  static final Map<String, FieldValidator> validatorSessionTeacherContentRegistry;
  static final Map<String, FieldValidator> validatorSessionStudentContentRegistry;
  static final Map<String, FieldConverter> converterRegistry;

  static {
    validatorRegistry = initializeValidators();
    validatorSessionRegistry = initializeSessionValidators();
    validatorSessionTeacherContentRegistry = initializeSessionTeacherContentValidators();
    validatorSessionStudentContentRegistry = initializeSessionStudentContentValidators();
    converterRegistry = initializeConverters();
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    converterMap.put(PRIOR_KNOWLEDGE, (FieldConverter::convertFieldToJson));
    converterMap.put(REFERENCE_LINKS, (FieldConverter::convertFieldJsonArrayToTextArray));
    converterMap.put(SESSIONS, (FieldConverter::convertFieldToJson));
    converterMap.put(COURSE_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(UNIT_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(LESSON_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    return Collections.unmodifiableMap(converterMap);
  }

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateString(value, 50000));
    validatorMap.put(GUIDING_QUESTIONS,
        (value) -> FieldValidator.validateStringIfPresent(value, 50000));
    validatorMap.put(PRIOR_KNOWLEDGE, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(ANTICIPATED_STRUGGLES,
        (value) -> FieldValidator.validateStringIfPresent(value, 50000));
    validatorMap.put(REFERENCE_LINKS, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(PACING_GUIDE_IN_HRS, FieldValidator::validateIntegerIfPresent);
    validatorMap.put(START_WEEK,
        (value) -> FieldValidator.validateIntegerWithRangeIfPresent(value, 0, 53));
    validatorMap.put(SESSIONS, FieldValidator::validateJsonArrayIfPresent);
    return Collections.unmodifiableMap(validatorMap);

  }

  private static Map<String, FieldValidator> initializeSessionValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 2000));
    validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateStringIfPresent(value, 50000));
    validatorMap.put(DURATION, FieldValidator::validateIntegerIfPresent);
    validatorMap.put(TEACHER_CONTENTS, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(STUDENT_CONTENTS, FieldValidator::validateJsonArrayIfPresent);
    return Collections.unmodifiableMap(validatorMap);
  }

  private static Map<String, FieldValidator> initializeSessionTeacherContentValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(CONTENT_ID, FieldValidator::validateUuid);
    validatorMap.put(CONTENT_FORMAT,
        (value) -> FieldValidator.validateStringAllowedValue(value, ALLOWED_TEACHER_CONTENT_FORMATS));
    return Collections.unmodifiableMap(validatorMap);
  }
  
  private static Map<String, FieldValidator> initializeSessionStudentContentValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(CONTENT_ID, FieldValidator::validateUuid);
    validatorMap.put(CONTENT_FORMAT,
        (value) -> FieldValidator.validateStringAllowedValue(value, ALLOWED_STUDENT_CONTENT_FORMATS));
    return Collections.unmodifiableMap(validatorMap);
  }

  public UUID getCourseId() {
    return UUID.fromString(this.getString(COURSE_ID));
  }

  public void setCourseId(String courseId) {
    setFieldUsingConverter(COURSE_ID, courseId);
  }

  public UUID getUnitId() {
    return UUID.fromString(this.getString(UNIT_ID));
  }

  public void setUnitId(String unitId) {
    setFieldUsingConverter(UNIT_ID, unitId);
  }

  public UUID getLessonId() {
    return UUID.fromString(this.getString(LESSON_ID));
  }

  public void setLessonId(String lessonId) {
    setFieldUsingConverter(LESSON_ID, lessonId);
  }


  private void setFieldUsingConverter(String fieldName, Object fieldValue) {
    FieldConverter fc = converterRegistry.get(fieldName);
    if (fc != null) {
      this.set(fieldName, fc.convertField(fieldValue));
    } else {
      this.set(fieldName, fieldValue);
    }
  }

}
