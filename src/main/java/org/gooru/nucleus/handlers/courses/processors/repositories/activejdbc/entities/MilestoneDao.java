package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;
import org.javalite.activejdbc.LazyList;

/**
 * @author ashish.
 */

public class MilestoneDao {

  private static final String MILESTONE_LIST_FOR_COURSE =
      "select distinct mlp.course_id, mlp.milestone_id, mlp.grade_id, mlp.grade_name, "
          + " mlp.grade_seq, mlp.fw_code, mlp.tx_subject_code from milestone_lesson_map mlp inner join lesson l on l.lesson_id = mlp.lesson_id "
          + " and l.course_id = mlp.course_id where l.is_deleted = false and mlp.course_id = ?::uuid order by grade_seq asc";

  public static final List<String> MILESTONE_SUMMARY_FIELDS = Arrays
      .asList("milestone_id", "grade_id", "grade_name", "grade_seq",
          "tx_subject_code");

  public LazyList<AJEntityMilestone> fetchMilestonesForCourse(String courseId) {
    // NOTE: Model is not completely hydrated as we do not need all fields
    return AJEntityMilestone
        .findBySQL(MILESTONE_LIST_FOR_COURSE, courseId);
  }

}
