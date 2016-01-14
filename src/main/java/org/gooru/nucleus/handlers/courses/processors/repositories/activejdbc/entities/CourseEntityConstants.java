package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

public class CourseEntityConstants {
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATE_AT = "updated_at";
  public static final String CREATOR_ID = "creator_id";
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

  public static final String[] NOTNULL_FIELDS = {TITLE};
  public static final String[] JSON_FIELDS = {AUDIENCE, METADATA, TAXONOMY, COLLABORATOR, CLASS_LIST};
  public static final String[] JSON_OBJECT_FIELDS = {METADATA, TAXONOMY};
  public static final String[] JSON_ARRAY_FIELDS = {AUDIENCE, COLLABORATOR, CLASS_LIST};
  public static final String[] ALL_FIELDS =
    {ID, TITLE, CREATOR_ID, ORIGINAL_CREATOR_ID, ORIGINAL_COURSE_ID, PUBLISH_DATE, THUMBNAIL, AUDIENCE, METADATA,
      TAXONOMY, COLLABORATOR, CLASS_LIST, VISIBLE_ON_PROFILE, CREATED_AT, UPDATE_AT};

}
