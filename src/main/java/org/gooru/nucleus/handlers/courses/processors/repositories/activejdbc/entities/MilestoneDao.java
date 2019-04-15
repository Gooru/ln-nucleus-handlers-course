package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

/**
 * @author ashish.
 */

public class MilestoneDao {

  private static final String MILESTONE_LIST_FOR_COURSE =
      "select distinct mlp.course_id, mlp.milestone_id, mlp.grade_id, mlp.grade_name, "
          + " mlp.grade_seq, mlp.fw_code, mlp.tx_subject_code from milestone_lesson_map mlp inner join lesson l on l.lesson_id = mlp.lesson_id "
          + " and l.course_id = mlp.course_id where l.is_deleted = false and mlp.course_id = ?::uuid order by grade_seq asc";

  private static final String MILESTONE_FETCH_FOR_COURSE =
      "select mlp.grade_id, mlp.grade_name, mlp.grade_seq, "
          + " mlp.fw_code, mlp.tx_subject_code, mlp.unit_id, u.title unit_title, u.sequence_id unit_sequence, "
          + " mlp.lesson_id, l.title lesson_title, l.sequence_id lesson_sequence, mlp.tx_domain_id, "
          + " mlp.tx_domain_code, mlp.tx_domain_seq, mlp.tx_comp_code, mlp.tx_comp_name, mlp.tx_comp_student_desc, mlp.tx_comp_seq "
          + " from milestone_lesson_map mlp inner join lesson l on l.lesson_id = mlp.lesson_id and l.course_id = mlp.course_id "
          + " inner join unit u on u.unit_id = mlp.unit_id and u.course_id = mlp.course_id "
          + " where l.is_deleted = false and u.is_deleted = false and mlp.course_id = ?::uuid and mlp.milestone_id = ?"
          + " order by grade_seq, u.sequence_Id, l.sequence_id asc";

  private static final String MILESTONE_EXISTS = "select exists (select 1 from milestone_lesson_map where milestone_id = ?)";

  public static final List<String> MILESTONE_SUMMARY_FIELDS = Arrays
      .asList("milestone_id", "grade_id", "grade_name", "grade_seq", "tx_subject_code");

  public LazyList<AJEntityMilestone> fetchMilestonesForCourse(String courseId) {
    // NOTE: Model is not completely hydrated as we do not need all fields
    return AJEntityMilestone
        .findBySQL(MILESTONE_LIST_FOR_COURSE, courseId);
  }

  public JsonArray fetchMilestoneBYIdForCourse(String courseId, String milestoneId) {
    List<Map> result = Base.findAll(MILESTONE_FETCH_FOR_COURSE, courseId, milestoneId);
    try {
      String resultString = new ObjectMapper().writeValueAsString(result);
      return new JsonArray(resultString);
    } catch (JsonProcessingException e) {
      // TODO: Fix this
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public boolean checkMilestoneExists(String milestoneId) {
    if (milestoneId != null) {
      Object objExists = Base.firstCell(MILESTONE_EXISTS, milestoneId);
      return Boolean.valueOf(String.valueOf(objExists));
    }
    return false;
  }

}
