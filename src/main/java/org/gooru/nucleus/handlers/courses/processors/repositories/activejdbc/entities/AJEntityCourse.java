package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("course")
public class AJEntityCourse extends Model {

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
  public static final List<String> ALL_FIELDS = Arrays.asList(ID, TITLE, OWNER_ID, CREATOR_ID, ORIGINAL_CREATOR_ID, MODIFIER_ID, ORIGINAL_COURSE_ID,
          PUBLISH_DATE, THUMBNAIL, AUDIENCE, METADATA, TAXONOMY, COLLABORATOR, CLASS_LIST, VISIBLE_ON_PROFILE, IS_DELETED, CREATED_AT, UPDATED_AT);
  public static final List<String> UPDATABLE_FIELDS =
          Arrays.asList(TITLE, PUBLISH_DATE, THUMBNAIL, AUDIENCE, METADATA, TAXONOMY, CLASS_LIST, VISIBLE_ON_PROFILE);
  public static final List<String> COLLABORATOR_FIELD = Arrays.asList(COLLABORATOR);
  
  public static final String SELECT_COLLABORATOR = "SELECT collaborator FROM course WHERE id = ?";
  public static final String SELECT_COURSE_TO_VALIDATE = "SELECT is_deleted, owner_id, publish_date, collaborator FROM course WHERE id = ?";
  public static final String SELECT_COURSE =
          "SELECT id, title, created_at, updated_at, owner_id, creator_id, original_creator_id, original_course_id, publish_date, thumbnail, audience,"
                  + " metadata, taxonomy, collaborator, class_list, visible_on_profile, is_deleted FROM course WHERE id = ? AND is_deleted = ?";

}
