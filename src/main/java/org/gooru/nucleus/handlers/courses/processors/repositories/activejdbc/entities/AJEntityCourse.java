package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Table("course")
public class AJEntityCourse extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityCourse.class);

  public static final String TABLE_COURSE = "course";
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String OWNER_ID = "owner_id";
  public static final String CREATOR_ID = "creator_id";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String ORIGINAL_CREATOR_ID = "original_creator_id";
  public static final String ORIGINAL_COURSE_ID = "original_course_id";
  public static final String PUBLISH_DATE = "publish_date";
  public static final String THUMBNAIL = "thumbnail";
  public static final String AUDIENCE = "audience";
  public static final String METADATA = "metadata";
  public static final String TAXONOMY = "taxonomy";
  public static final String COLLABORATOR = "collaborator";
  public static final String CLASS_LIST = "class_list";
  public static final String VISIBLE_ON_PROFILE = "visible_on_profile";
  public static final String IS_DELETED = "is_deleted";

  public static final List<String> NOTNULL_FIELDS = Arrays.asList(TITLE);
  public static final List<String> JSON_FIELDS = Arrays.asList(AUDIENCE, METADATA, TAXONOMY, COLLABORATOR, CLASS_LIST);
  public static final List<String> JSON_OBJECT_FIELDS = Arrays.asList(METADATA, TAXONOMY);
  public static final List<String> JSON_ARRAY_FIELDS = Arrays.asList(AUDIENCE, COLLABORATOR, CLASS_LIST);
  public static final List<String> ALL_FIELDS = Arrays
    .asList(ID, TITLE, OWNER_ID, CREATOR_ID, ORIGINAL_CREATOR_ID, MODIFIER_ID, ORIGINAL_COURSE_ID, PUBLISH_DATE, THUMBNAIL, AUDIENCE, METADATA,
      TAXONOMY, COLLABORATOR, CLASS_LIST, VISIBLE_ON_PROFILE, IS_DELETED, CREATED_AT, UPDATED_AT);

  public static final List<String> INSERT_FORBIDDEN_FIELDS =
    Arrays.asList(ID, CREATED_AT, UPDATED_AT, OWNER_ID, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_COURSE_ID, PUBLISH_DATE, IS_DELETED);
  public static final List<String> UPDATE_FORBIDDEN_FIELDS =
    Arrays.asList(ID, CREATED_AT, UPDATED_AT, OWNER_ID, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_COURSE_ID, PUBLISH_DATE, IS_DELETED);
  public static final List<String> COLLABORATOR_FIELD = Arrays.asList(COLLABORATOR);
  public static final List<String> UNIT_MOVE_NOTNULL_FIELDS = Arrays.asList("course_id", "unit_id");

  public static final String SELECT_COLLABORATOR = "SELECT collaborator FROM course WHERE id = ?::uuid";
  //TODO: update it to include is_deleted
  public static final String SELECT_COURSE_TO_VALIDATE =
    "SELECT id, owner_id, publish_date, collaborator FROM course WHERE id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_COURSE =
    "SELECT id, title, created_at, updated_at, owner_id, creator_id, original_creator_id, original_course_id, publish_date, thumbnail, audience," +
      " metadata, taxonomy, collaborator, class_list, visible_on_profile, is_deleted FROM course WHERE id = ?::uuid AND is_deleted = ?";

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
    setPGObject(ID, UUID_TYPE, courseId);
  }

  public void setCollaborator(String collaborator) {
    setPGObject(COLLABORATOR, JSONB_TYPE, collaborator);
  }

  // NOTE:
  // We do not deal with nested objects, only first level ones
  // We do not check for forbidden fields, it should be done before this
  public void setAllFromJson(JsonObject input) {
    input.getMap().forEach((s, o) -> {
      // Note that special UUID cases for modifier and creator should be handled internally and not via map, so we do not care
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

}
