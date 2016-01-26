package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("content")
public class AJEntityContent extends Model {

  public static final String ID = "id";
  public static final String IS_DELETED = "is_deleted";
  
  public static final String SELECT_CONTENT_ASSOCIATED_WITH_COURSE = "SELECT id FROM content WHERE course_id = ? AND is_deleted = ?";
  public static final String SELECT_CONTENT_ASSOCIATED_WITH_UNIT = "SELECT id FROM content WHERE unit_id = ? AND is_deleted = ?";
  public static final String SELECT_CONTENT_ASSOCIATED_WITH_LESSON = "SELECT id FROM content WHERE lesson_id = ? AND is_deleted = ?";
}
