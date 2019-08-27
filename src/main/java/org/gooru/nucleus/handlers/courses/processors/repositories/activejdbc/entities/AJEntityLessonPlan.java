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
import io.vertx.core.json.JsonObject;

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
  public static final String PACING_GUIDE_IN_DAYS = "pacing_guide_in_days";
  public static final String START_WEEK = "start_week";
  public static final String SESSIONS = "sessions";
  public static final String CONTENTS = "contents";
  public static final String CONTENT_FORMAT = "content_format";
  public static final String CONTENT_ID = "content_id";
  private static final Set<String> ALLOWED_CONTENT_FORMATS =
      new HashSet<>(Arrays.asList("assessment", "collection", "assessment-external",
          "collection-external", "offline-activity", "question", "resource"));

  static final Map<String, FieldValidator> validatorRegistry;
  static final Map<String, FieldValidator> validatorSessionRegistry;
  static final Map<String, FieldValidator> validatorSessionContentRegistry;
  static final Map<String, FieldConverter> converterRegistry;

  static {
    validatorRegistry = initializeValidators();
    validatorSessionRegistry = initializeSessionValidators();
    validatorSessionContentRegistry = initializeSessionContentValidators();
    converterRegistry = initializeConverters();
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    converterMap.put(PRIOR_KNOWLEDGE, (FieldConverter::convertFieldToJson));
    converterMap.put(ANTICIPATED_STRUGGLES, (FieldConverter::convertFieldToJson));
    converterMap.put(REFERENCE_LINKS, (FieldConverter::convertFieldToJson));
    return Collections.unmodifiableMap(converterMap);
  }

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateString(value, 50000));
    validatorMap.put(GUIDING_QUESTIONS,
        (value) -> FieldValidator.validateStringIfPresent(value, 50000));
    validatorMap.put(PRIOR_KNOWLEDGE, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(ANTICIPATED_STRUGGLES, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(REFERENCE_LINKS, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(PACING_GUIDE_IN_DAYS, FieldValidator::validateIntegerIfPresent);
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
    validatorMap.put(CONTENTS, FieldValidator::validateJsonArrayIfPresent);
    return Collections.unmodifiableMap(validatorMap);
  }

  private static Map<String, FieldValidator> initializeSessionContentValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(CONTENT_ID, FieldValidator::validateUuid);
    validatorMap.put(CONTENT_FORMAT,
        (value) -> FieldValidator.validateStringAllowedValue(value, ALLOWED_CONTENT_FORMATS));
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

  public String getDescription() {
    return this.getString(DESCRIPTION);
  }

  public String getGuidingQuestions() {
    return this.getString(GUIDING_QUESTIONS);
  }

  public String[] getPriorKnowledge() {
    return (String[]) this.get(PRIOR_KNOWLEDGE);
  }

  public String[] getAnticipatedStruggles() {
    return (String[]) this.get(ANTICIPATED_STRUGGLES);
  }

  public String[] getReferenceLinks() {
    return (String[]) this.get(REFERENCE_LINKS);
  }

  public int getPacingGuideInDays() {
    return this.getInteger(PACING_GUIDE_IN_DAYS);
  }

  public int getStartWeek() {
    return this.getInteger(START_WEEK);
  }

  public JsonObject getSession() {
    return (JsonObject) this.get(SESSIONS);
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
