package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public interface CourseRepo {

  public String ID = "id";
  public String TITLE = "title";
  public String CREATED_AT = "created_at";
  public String UPDATE_AT = "updated_at";
  public String CREATOR_ID = "creator_id";
  public String ORIGINAL_CREATOR_ID = "original_creator_id";
  public String ORIGINAL_COURSE_ID = "original_course_id";
  public String PUBLISH_DATE = "publish_date";
  public String THUMBNAIL = "thumbnail";
  public String AUDIENCE = "audience";
  public String METADATA = "metadata";
  public String TAXONOMY = "taxonomy";
  public String COLLABORATOR = "collaborator";
  public String CLASS_LIST = "class_list";
  public String VISIBLE_ON_PROFILE = "visible_on_profile";
  public String IS_DELETED = "is_deleted";

  public String[] NOTNULL_FIELDS = { TITLE };
  public String[] JSON_FIELDS = { AUDIENCE, METADATA, TAXONOMY, COLLABORATOR, CLASS_LIST };
  public String[] JSON_OBJECT_FIELDS = { METADATA, TAXONOMY };
  public String[] JSON_ARRAY_FIELDS = { AUDIENCE, COLLABORATOR, CLASS_LIST };
  public String[] ALL_FIELDS = {ID, TITLE, CREATOR_ID, ORIGINAL_CREATOR_ID, ORIGINAL_COURSE_ID, PUBLISH_DATE,
      THUMBNAIL, AUDIENCE, METADATA, TAXONOMY, COLLABORATOR, CLASS_LIST, VISIBLE_ON_PROFILE, IS_DELETED, CREATED_AT, UPDATE_AT};

  MessageResponse fetchCourse();

  MessageResponse createCourse();

  MessageResponse updateCourse();

  MessageResponse deleteCourse();

  MessageResponse copyCourse();

  MessageResponse reorderUnitInCourse();
}
