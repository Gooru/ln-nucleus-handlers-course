package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("collection")
public class AJEntityCollection extends Model {
  
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String IS_DELETED = "is_deleted";
  
  public static final List<String> COLLECTION_SUMMARY_FIELDS = Arrays.asList(ID, TITLE);
  
  public static final String SELECT_COLLECTION_SUMMARY =
          "SELECT id, title FROM collection WHERE lesson_id = ?::uuid AND is_deleted = ?";
}
