package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table("course_unit")
@IdName("unit_id")
public class AJEntityUnit extends Model {

  public static final String TABLE_UNIT = "course_unit";
  public static final String UNIT_ID = "unit_id";
  public static final String COURSE_ID = "course_id";
  public static final String TITLE = "title";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
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
  public static final List<String> ALL_FIELDS = Arrays.asList(UNIT_ID, COURSE_ID, TITLE, CREATED_AT, UPDATED_AT, CREATOR_ID, MODIFIER_ID,
          ORIGINAL_CREATOR_ID, ORIGINAL_UNIT_ID, BIG_IDEAS, ESSENTIAL_QUESTIONS, METADATA, TAXONOMY, SEQUENCE_ID, IS_DELETED);

  public static final List<String> UPDATABLE_FIELDS =
          Arrays.asList(TITLE, BIG_IDEAS, ESSENTIAL_QUESTIONS, METADATA, TAXONOMY, SEQUENCE_ID, IS_DELETED);

  public static final String[] UNIT_SUMMARY_FIELDS = { UNIT_ID, TITLE, SEQUENCE_ID };

  public static final String SELECT_UNIT =
          "SELECT course_id, unit_id, title, created_at, updated_at, creator_id, modifier_id, original_creator_id, original_unit_id,"
                  + " big_ideas, essential_questions, metadata, taxonomy, sequence_id, is_deleted FROM course_unit WHERE course_id = ? AND unit_id = ? AND is_deleted = ?";

  public static final String SELECT_UNIT_TO_VALIDATE = "SELECT is_deleted, creator_id FROM course_unit WHERE unit_id = ?";
  public static final String SELECT_UNIT_SUMMARY = "SELECT unit_id, title, sequence_id FROM course_unit WHERE course_id = ? AND is_deleted = ?";
  public static final String SELECT_UNIT_MAX_SEQUENCEID = "SELECT max(sequence_id) FROM course_unit WHERE course_id = ?";
}