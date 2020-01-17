package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Table("lesson")
@IdName("lesson_id")
public class AJEntityLesson extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityLesson.class);
  public static final String TABLE_LESSON = "lesson";
  public static final String LESSON_ID = "lesson_id";
  public static final String UNIT_ID = "unit_id";
  public static final String COURSE_ID = "course_id";
  public static final String TITLE = "title";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String OWNER_ID = "owner_id";
  public static final String CREATOR_ID = "creator_id";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String ORIGINAL_CREATOR_ID = "original_creator_id";
  public static final String ORIGINAL_LESSON_ID = "original_lesson_id";
  public static final String METADATA = "metadata";
  public static final String TAXONOMY = "taxonomy";
  public static final String SEQUENCE_ID = "sequence_id";
  public static final String IS_DELETED = "is_deleted";
  public static final String CREATOR_SYSTEM = "creator_system";
  public static final String TENANT = "tenant";
  public static final String TENANT_ROOT = "tenant_root";
  public static final String ID = "id";
  public static final String AGGREGATED_TAXONOMY = "aggregated_taxonomy";
  public static final String AGGREGATED_GUT_CODES = "aggregated_gut_codes";
  public static final String LESSON_PLAN = "lesson_plan";

  public static final String LESSON_SUMMARY = "lesson_summary";
  public static final String LESSON_COUNT = "lesson_count";
  public static final String LESSONS = "lessons";
  public static final String PRIMARY_LANGUAGE = "primary_language";

  public static final List<String> NOTNULL_FIELDS = Arrays.asList(TITLE);
  public static final List<String> JSON_FIELDS = Arrays.asList(METADATA, TAXONOMY);
  public static final List<String> ALL_FIELDS =
      Arrays.asList(LESSON_ID, UNIT_ID, COURSE_ID, TITLE, CREATED_AT, UPDATED_AT, CREATOR_ID,
          MODIFIER_ID, OWNER_ID,
          ORIGINAL_CREATOR_ID, ORIGINAL_LESSON_ID, METADATA, TAXONOMY, SEQUENCE_ID, CREATOR_SYSTEM,
          AGGREGATED_TAXONOMY, PRIMARY_LANGUAGE);
  public static final List<String> LESSON_SUMMARY_FIELDS = Arrays
      .asList(LESSON_ID, TITLE, SEQUENCE_ID);

  public static final String SELECT_LESSON_TO_VALIDATE =
      "SELECT lesson_id, unit_id, course_id, taxonomy FROM lesson WHERE lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_LESSON =
      "SELECT lesson_id, unit_id, course_id, title, created_at, updated_at, owner_id, creator_id, modifier_id, original_creator_id, "
          + "original_lesson_id, metadata, taxonomy, sequence_id, creator_system, aggregated_taxonomy, primary_language FROM lesson WHERE lesson_id = ?::uuid"
          + " AND unit_id = ?::uuid AND course_id = ?::uuid and is_deleted = ?";
  public static final String SELECT_LESSON_SUMMARY =
      "SELECT lesson_id, title, sequence_id FROM lesson WHERE unit_id = ?::uuid AND course_id = ?::uuid AND is_deleted = ? order by sequence_id asc";
  public static final String SELECT_LESSON_MAX_SEQUENCEID =
      "SELECT max(sequence_id) FROM lesson WHERE course_id = ?::uuid AND unit_id = ?::uuid";
  public static final String SELECT_LESSON_OF_COURSE =
      "SELECT lesson_id FROM lesson WHERE unit_id = ?::uuid AND course_id = ?::uuid AND"
          + " is_deleted = ?";
  public static final String REORDER_QUERY =
      "UPDATE lesson SET sequence_id = ?, modifier_id = ?::uuid, updated_at = now() WHERE lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id"
          + " = ?::uuid AND is_deleted = ?";
  public static final String SELECT_LESSON_COUNT_MULTIPLE =
      "SELECT count(lesson_id) as lesson_count, unit_id FROM lesson WHERE course_id = ?::uuid AND is_deleted = false"
          + " GROUP BY unit_id";
  public static final String SELECT_LESSON_BY_COURSE =
      "SELECT lesson_id, unit_id, title, sequence_id FROM lesson WHERE course_id = ?::uuid AND is_deleted = false";
  
  public static final String SELECT_LESSON_PLAN_BY_LESSON =
      "SELECT   jsonb_array_length(sessions) AS lp_session_count, pacing_guide_in_secs as lp_pacing_guide_in_secs, lesson_id  FROM lesson_plan WHERE lesson_id = ANY(?::uuid[]) AND unit_id = ?::uuid AND"
          + " course_id = ?::uuid";

  public static final Set<String> INSERTABLE_FIELDS = new HashSet<>(Arrays
      .asList(TITLE, METADATA, TAXONOMY, CREATOR_SYSTEM, PRIMARY_LANGUAGE));
  public static final Set<String> UPDATABLE_FIELDS = new HashSet<>(Arrays.asList(TITLE, METADATA, TAXONOMY, PRIMARY_LANGUAGE));
  public static final Set<String> MANDATORY_FIELDS = new HashSet<>(Arrays.asList(TITLE));

  public static final List<String> COLLECTION_MOVE_NOTNULL_FIELDS = Arrays.asList("collection_id");

  public static final String UUID_TYPE = "uuid";
  public static final String JSONB_TYPE = "jsonb";

  private static final Map<String, FieldValidator> validatorRegistry;
  private static final Map<String, FieldConverter> converterRegistry;

  static {
      validatorRegistry = initializeValidators();
      converterRegistry = initializeConverters();
  }

  private static Map<String, FieldConverter> initializeConverters() {
      Map<String, FieldConverter> converterMap = new HashMap<>();
      converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
      converterMap.put(METADATA, (FieldConverter::convertFieldToJson));
      converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
      converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
      converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
      converterMap.put(OWNER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
      converterMap.put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
      converterMap.put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));

      return Collections.unmodifiableMap(converterMap);
  }

  private static Map<String, FieldValidator> initializeValidators() {
      Map<String, FieldValidator> validatorMap = new HashMap<>();
      validatorMap.put(ID, (FieldValidator::validateUuid));
      validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
      validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
      validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
      validatorMap.put(TENANT, (FieldValidator::validateUuid));
      validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
      validatorMap.put(PRIMARY_LANGUAGE, FieldValidator::validateLanguageIfPresent);
      return Collections.unmodifiableMap(validatorMap);
  }

  public void setModifierId(String modifierId) {
    setPGObject(MODIFIER_ID, UUID_TYPE, modifierId);
  }

  public void setCreatorId(String creatorId) {
    setPGObject(CREATOR_ID, UUID_TYPE, creatorId);
  }

  public void setOwnerId(String ownerId) {
    setPGObject(OWNER_ID, UUID_TYPE, ownerId);
  }

  public void setCourseId(String courseId) {
    setPGObject(COURSE_ID, UUID_TYPE, courseId);
  }

  public void setUnitId(String unitId) {
    setPGObject(UNIT_ID, UUID_TYPE, unitId);
  }

  public void setLessonId(String lessonId) {
    setPGObject(LESSON_ID, UUID_TYPE, lessonId);
  }

  public void setTenant(String tenantId) {
    setPGObject(TENANT, UUID_TYPE, tenantId);
  }

  public void setTenantRoot(String tenantRoot) {
    if (tenantRoot != null && !tenantRoot.isEmpty()) {
      setPGObject(TENANT_ROOT, UUID_TYPE, tenantRoot);
    }
  }

  // NOTE:
  // We do not deal with nested objects, only first level ones
  // We do not check for forbidden fields, it should be done before this
  public void setAllFromJson(JsonObject input) {
    input.getMap().forEach((s, o) -> {
      // Note that special UUID cases for modifier and creator should be
      // handled internally and not via map, so we do not care
      if (o instanceof JsonObject) {
        this.setPGObject(s, JSONB_TYPE, o.toString());
      } else if (o instanceof JsonArray) {
        this.setPGObject(s, JSONB_TYPE, o.toString());
      } else {
        this.set(s, o);
      }
    });
  }

  private void setPGObject(String field, String type, String value) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value);
      this.set(field, pgObject);
    } catch (SQLException e) {
      LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
      this.errors().put(field, value);
    }
  }
  
  public static FieldSelector createFieldSelector() {
      return new FieldSelector() {
          @Override
          public Set<String> allowedFields() {
              return Collections.unmodifiableSet(INSERTABLE_FIELDS);
          }

          @Override
          public Set<String> mandatoryFields() {
              return Collections.unmodifiableSet(MANDATORY_FIELDS);
          }
      };
  }
  
  public static FieldSelector editFieldSelector() {
      return new FieldSelector() {
          @Override
          public Set<String> allowedFields() {
              return Collections.unmodifiableSet(UPDATABLE_FIELDS);
          }
      };
  }
  
  public static ValidatorRegistry getValidatorRegistry() {
      return new LessonValidatorRegistry();
  }

  public static ConverterRegistry getConverterRegistry() {
      return new LessonConvertorRegistry();
  }

  private static class LessonValidatorRegistry implements ValidatorRegistry {
      @Override
      public FieldValidator lookupValidator(String fieldName) {
          return validatorRegistry.get(fieldName);
      }
  }

  private static class LessonConvertorRegistry implements ConverterRegistry {
      @Override
      public FieldConverter lookupConverter(String fieldName) {
          return converterRegistry.get(fieldName);
      }
  }
}
