package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Table("collection")
public class AJEntityCollection extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityCollection.class);

  public static final String ID = "id";
  public static final String COURSE_ID = "course_id";
  public static final String UNIT_ID = "unit_id";
  public static final String LESSON_ID = "lesson_id";
  public static final String TITLE = "title";
  public static final String FORMAT = "format";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String OWNER_ID = "owner_id";
  public static final String COLLABORATOR = "collaborator";
  public static final String SEQUENCE_ID = "sequence_id";
  public static final String IS_DELETED = "is_deleted";

  public static final List<String> COLLECTION_SUMMARY_FIELDS = Arrays.asList(ID, TITLE, FORMAT, SEQUENCE_ID);

  public static final String SELECT_COLLECTION_SUMMARY =
    "SELECT id, title, format, sequence_id FROM collection WHERE lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND "
    + "is_deleted = ? order by sequence_id asc";
  public static final String SELECT_COLLECTION_TO_MOVE =
    "SELECT id, course_id, unit_id, lesson_id, owner_id, collaborator FROM collection WHERE id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_COLLECTION_OF_COURSE =
    "SELECT id FROM collection WHERE lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND is_deleted = ?";
  public static final String REORDER_QUERY =
    "UPDATE collection SET sequence_id = ?, modifier_id = ?::uuid, updated_at = now() WHERE id = ?::uuid AND lesson_id = ?::uuid AND unit_id = " +
      "?::uuid AND course_id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_COLLECTION_MAX_SEQUENCEID = "SELECT max(sequence_id) FROM collection WHERE lesson_id = ?::uuid";

  public static final String SELECT_COLLECTION_CONTENT_COUNT = "SELECT count(id) as contentCount, content_format, collection_id FROM content WHERE"
    +" collection_id = ANY(?::uuid[]) AND course_id = ?::uuid AND unit_id = ?::uuid AND lesson_id = ?::uuid AND is_deleted = false GROUP BY collection_id, content_format";
  //select count(id), content_format, collection_id from content where collection_id IN ('259b732c-2672-4780-9616-2c7a35d2d526', '2daec488-370c-424f-b0e9-062c5fd274be')^Croup by collection_id, content_format;
  public static final String UUID_TYPE = "uuid";
  public static final String JSONB_TYPE = "jsonb";

  public static final List<String> COLLECTION_MOVE_NOTNULL_FIELDS = Arrays.asList(COURSE_ID, UNIT_ID, LESSON_ID);

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
