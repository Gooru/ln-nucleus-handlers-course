package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Table("collection")
public class AJEntityCollection extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityCollection.class);

  public static final String ID = "id";
  public static final String COURSE_ID = "course_id";
  public static final String UNIT_ID = "unit_id";
  public static final String LESSON_ID = "lesson_id";
  public static final String TITLE = "title";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String OWNER_ID = "owner_id";
  public static final String IS_DELETED = "is_deleted";

  public static final List<String> COLLECTION_SUMMARY_FIELDS = Arrays.asList(ID, TITLE);

  public static final String SELECT_COLLECTION_SUMMARY = "SELECT id, title FROM collection WHERE lesson_id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_COLLECTION_TO_MOVE =
          "SELECT id, course_id, unit_id, lesson_id, owner_id, collaborator FROM collection WHERE id = ?::uuid AND is_deleted = ?";

  public static final String UUID_TYPE = "uuid";
  public static final String JSONB_TYPE = "jsonb";

  public void setCourseId(String courseId) {
    setPGObject(COURSE_ID, UUID_TYPE, courseId);
  }

  public void setUnitId(String unitId) {
    setPGObject(UNIT_ID, UUID_TYPE, unitId);
  }

  public void setLessonId(String lessonId) {
    setPGObject(LESSON_ID, UUID_TYPE, lessonId);
  }

  public void setModifierId(String modifierId) {
    setPGObject(MODIFIER_ID, UUID_TYPE, modifierId);
  }

  public void setOwnerId(String ownerId) {
    setPGObject(OWNER_ID, UUID_TYPE, ownerId);
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
