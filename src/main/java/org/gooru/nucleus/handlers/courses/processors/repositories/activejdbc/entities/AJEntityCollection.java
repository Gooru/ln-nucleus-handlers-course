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
  public static final String URL = "url";
  public static final String FORMAT = "format";
  public static final String SUBFORMAT = "subformat";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String OWNER_ID = "owner_id";
  public static final String COLLABORATOR = "collaborator";
  public static final String SEQUENCE_ID = "sequence_id";
  public static final String IS_DELETED = "is_deleted";
  public static final String THUMBNAIL = "thumbnail";
  public static final String TAXONOMY = "taxonomy";

  public static final String COLLECTION_SUMMARY = "collection_summary";
  public static final String COLLECTION_COUNT = "collection_count";
  public static final String ASSESSMENT_COUNT = "assessment_count";
  public static final String EXT_ASSESSMENT_COUNT = "external_assessment_count";
  public static final String EXT_COLLECTION_COUNT = "external_collection_count";
  public static final String OA_COUNT = "oa_count";
  public static final String ASSESSMENTS = "assessments";
  public static final String ASSESSMENTS_EXTERNAL = "assessments-external";
  public static final String COLLECTIONS = "collections";
  public static final String COLLECTIONS_EXTERNAL = "collections-external";
  public static final String OFFLINE_ACTIVITY = "offline-activity";

  public static final List<String> COLLECTION_SUMMARY_FIELDS =
      Arrays.asList(ID, TITLE, FORMAT, SEQUENCE_ID, THUMBNAIL, URL, SUBFORMAT);

  public static final String SELECT_COLLECTION_SUMMARY =
      "SELECT id, title, format, sequence_id, thumbnail, url, subformat FROM collection WHERE lesson_id = ?::uuid AND unit_id = ?::uuid"
          + " AND course_id = ?::uuid AND is_deleted = ? order by sequence_id asc";
  public static final String SELECT_COLLECTION_TO_MOVE =
      "SELECT id, course_id, unit_id, lesson_id, owner_id, collaborator, taxonomy FROM collection WHERE id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_COLLECTION_OF_COURSE =
      "SELECT id FROM collection WHERE lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND is_deleted = ?";
  public static final String REORDER_QUERY =
      "UPDATE collection SET sequence_id = ?, modifier_id = ?::uuid, updated_at = now() WHERE id = ?::uuid AND lesson_id = ?::uuid AND unit_id = "
          + "?::uuid AND course_id = ?::uuid AND is_deleted = ?";
  public static final String SELECT_COLLECTION_MAX_SEQUENCEID =
      "SELECT max(sequence_id) FROM collection WHERE course_id = ?::uuid AND unit_id = ?::uuid AND lesson_id = ?::uuid";
  public static final String SELECT_COLLECTION_TO_VALIDATE =
      "SELECT id, taxonomy FROM collection where id = ?::uuid AND is_deleted = false AND course_id = ?::uuid AND unit_id = ?::uuid AND"
          + " lesson_id = ?::uuid";

  public static final String SELECT_COLLECTION_ASSESSMET_COUNT_BY_LESSON =
      "SELECT count(id) as collection_count, format, lesson_id FROM collection WHERE lesson_id = ANY(?::uuid[]) AND unit_id = ?::uuid AND"
          + " course_id = ?::uuid AND is_deleted = false GROUP BY lesson_id, format";


  public static final String SELECT_ASSESSMENTS_BY_COURSE =
      "SELECT id, unit_id, lesson_id, title, format, subformat, sequence_id FROM collection where format = 'assessment'::content_container_type AND"
          + " course_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_COLLECTIONS_BY_COURSE =
      "SELECT id, unit_id, lesson_id, title, format, subformat, sequence_id FROM collection where format = 'collection'::content_container_type AND"
          + " course_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_EXT_ASSESSMENTS_BY_COURSE =
      "SELECT id, unit_id, lesson_id, title, format, subformat, sequence_id FROM collection where format = 'assessment-external'::content_container_type AND"
          + " course_id = ?::uuid AND is_deleted = false";

  public static final String SELECT_EXT_COLLECTIONS_BY_COURSE =
      "SELECT id, unit_id, lesson_id, title, format, subformat, sequence_id FROM collection where format = 'collection-external'::content_container_type AND"
          + " course_id = ?::uuid AND is_deleted = false";

  public static final String UPDATE_COLLECTION_REMOVE_CUL =
      "course_id = null, unit_id = null, lesson_id = null";
  public static final String UPDATE_COLLECTION_REMOVE_CUL_WHERE = "id = ?::uuid";

  
  public static final String UUID_TYPE = "uuid";
  public static final String JSONB_TYPE = "jsonb";

  public static final List<String> COLLECTION_MOVE_NOTNULL_FIELDS =
      Arrays.asList(COURSE_ID, UNIT_ID, LESSON_ID);
  public static final List<String> ASSESSMENT_BY_COURSE_FIELDS =
      Arrays.asList(ID, TITLE, FORMAT, SUBFORMAT, SEQUENCE_ID);
  public static final List<String> COLLECTION_BY_COURSE_FIELDS =
      Arrays.asList(ID, TITLE, FORMAT, SUBFORMAT, SEQUENCE_ID);

  public static final String FORMAT_COLLECTION = "collection";
  public static final String FORMAT_ASSESSMENT = "assessment";
  public static final String FORMAT_EXT_ASSESSMENT = "assessment-external";
  public static final String FORMAT_EXT_COLLECTION = "collection-external";
  public static final String FORMAT_OA = "offline-activity";

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

  public boolean isOfflineActivity() {
    return this.getString(FORMAT).equalsIgnoreCase(OFFLINE_ACTIVITY);
  }

}
