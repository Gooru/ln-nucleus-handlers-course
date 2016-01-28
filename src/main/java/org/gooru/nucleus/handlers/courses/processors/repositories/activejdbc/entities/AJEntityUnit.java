package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Table("course_unit")
@IdName("unit_id")
public class AJEntityUnit extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityUnit.class);

  public static final String TABLE_UNIT = "course_unit";
  public static final String UNIT_ID = "unit_id";
  public static final String COURSE_ID = "course_id";
  public static final String TITLE = "title";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String OWNER_ID = "owner_id";
  public static final String CREATOR_ID = "creator_id";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String ORIGINAL_CREATOR_ID = "original_creator_id";
  public static final String ORIGINAL_UNIT_ID = "original_unit_id";
  public static final String BIG_IDEAS = "big_ideas";
  public static final String ESSENTIAL_QUESTIONS = "essential_questions";
  public static final String METADATA = "metadata";
  public static final String TAXONOMY = "taxonomy";
  public static final String SEQUENCE_ID = "sequence_id";
  public static final String IS_DELETED = "is_deleted";

  public static final List<String> NOTNULL_FIELDS = Arrays.asList(TITLE, BIG_IDEAS, ESSENTIAL_QUESTIONS);
  public static final List<String> JSON_FIELDS = Arrays.asList(METADATA, TAXONOMY);
  public static final List<String> JSON_OBJECT_FIELDS = Arrays.asList(METADATA, TAXONOMY);
  public static final List<String> ALL_FIELDS = Arrays.asList(UNIT_ID, COURSE_ID, TITLE, CREATED_AT, UPDATED_AT, OWNER_ID, CREATOR_ID, MODIFIER_ID,
          ORIGINAL_CREATOR_ID, ORIGINAL_UNIT_ID, BIG_IDEAS, ESSENTIAL_QUESTIONS, METADATA, TAXONOMY, SEQUENCE_ID, IS_DELETED);

  public static final List<String> UPDATABLE_FIELDS = Arrays.asList(TITLE, BIG_IDEAS, ESSENTIAL_QUESTIONS, METADATA, TAXONOMY);

  public static final List<String> UNIT_SUMMARY_FIELDS = Arrays.asList(UNIT_ID, TITLE, SEQUENCE_ID);

  public static final List<String> INSERT_FORBIDDEN_FIELDS = Arrays.asList(UNIT_ID, COURSE_ID, CREATED_AT, UPDATED_AT, OWNER_ID, CREATOR_ID,
          MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_UNIT_ID, SEQUENCE_ID, IS_DELETED);
  public static final List<String> UPDATE_FORBIDDEN_FIELDS = Arrays.asList(UNIT_ID, COURSE_ID, CREATED_AT, UPDATED_AT, OWNER_ID, CREATOR_ID,
          MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_UNIT_ID, SEQUENCE_ID, IS_DELETED);

  public static final String SELECT_UNIT =
          "SELECT course_id, unit_id, title, created_at, updated_at, owner_id, creator_id, modifier_id, original_creator_id, original_unit_id,"
                  + " big_ideas, essential_questions, metadata, taxonomy, sequence_id, is_deleted FROM course_unit WHERE course_id = ?::uuid AND"
                  + " unit_id = ?::uuid AND is_deleted = ?";

  public static final String SELECT_UNIT_TO_VALIDATE = "SELECT is_deleted, creator_id FROM course_unit WHERE unit_id = ?::uuid";
  public static final String SELECT_UNIT_SUMMARY =
          "SELECT unit_id, title, sequence_id FROM course_unit WHERE course_id = ?::uuid AND is_deleted = ? order by sequence_id asc";
  public static final String SELECT_UNIT_MAX_SEQUENCEID = "SELECT max(sequence_id) FROM course_unit WHERE course_id = ?::uuid";

  public static final String UUID_TYPE = "uuid";
  public static final String JSONB_TYPE = "jsonb";

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

  // NOTE:
  // We do not deal with nested objects, only first level ones
  // We do not check for forbidden fields, it should be done before this
  public void setAllFromJson(JsonObject input) {
    input.getMap().forEach((s, o) -> {
      // Note that special UUID cases for modifier and creator should be handled
      // internally and not via map, so we do not care
      if (o instanceof JsonObject) {
        this.setPGObject(s, JSONB_TYPE, ((JsonObject) o).toString());
      } else if (o instanceof JsonArray) {
        this.setPGObject(s, JSONB_TYPE, ((JsonArray) o).toString());
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
}