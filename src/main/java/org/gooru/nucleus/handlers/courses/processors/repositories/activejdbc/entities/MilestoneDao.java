package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public class MilestoneDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneDao.class);

  private static final String MILESTONE_LIST_FOR_COURSE =
      "select distinct mlp.course_id, mlp.milestone_id, mlp.grade_id, mlp.grade_name, "
          + " mlp.grade_seq, mlp.fw_code, mlp.tx_subject_code from milestone_lesson_map mlp inner join lesson l on l.lesson_id = mlp.lesson_id "
          + " and l.course_id = mlp.course_id where l.is_deleted = false and mlp.course_id = ?::uuid and fw_code = ? order by grade_seq asc";

  private static final String MILESTONE_LIST_FOR_COURSE_EXCLUDING_SPECIFIED_LESSONS =
      "select distinct mlp.course_id, mlp.milestone_id, mlp.grade_id, mlp.grade_name, "
          + " mlp.grade_seq, mlp.fw_code, mlp.tx_subject_code from milestone_lesson_map mlp inner join lesson l on l.lesson_id = mlp.lesson_id "
          + " and l.course_id = mlp.course_id where l.is_deleted = false and mlp.course_id = ?::uuid and fw_code = ? "
          + " and l.lesson_id != all (?::uuid[]) order by grade_seq asc";

  private static final String FETCH_USER_RESCOPED_CONTENT = "select skipped_content from user_rescoped_content where user_id = ?::uuid and "
      + " course_id = ?::uuid and class_id = ?::uuid ";

  private static final String MILESTONE_FETCH_FOR_COURSE =
      "select mlp.grade_id, mlp.grade_name, mlp.grade_seq, "
          + " mlp.fw_code, mlp.tx_subject_code, mlp.unit_id, u.title unit_title, u.sequence_id unit_sequence, "
          + " mlp.lesson_id, l.title lesson_title, l.sequence_id lesson_sequence, mlp.tx_domain_id, "
          + " mlp.tx_domain_code, mlp.tx_domain_name, mlp.tx_domain_seq, mlp.tx_comp_code, mlp.tx_comp_name, mlp.tx_comp_student_desc, mlp.tx_comp_seq "
          + " from milestone_lesson_map mlp inner join lesson l on l.lesson_id = mlp.lesson_id and l.course_id = mlp.course_id "
          + " inner join unit u on u.unit_id = mlp.unit_id and u.course_id = mlp.course_id "
          + " where l.is_deleted = false and u.is_deleted = false and mlp.course_id = ?::uuid and mlp.milestone_id = ?"
          + " order by grade_seq, u.sequence_Id, l.sequence_id asc";

  private static final String MILESTONE_EXISTS = "select exists (select 1 from milestone_lesson_map where milestone_id = ?)";

  public static final List<String> MILESTONE_SUMMARY_FIELDS = Arrays
      .asList("milestone_id", "grade_id", "grade_name", "grade_seq", "tx_subject_code");

  public LazyList<AJEntityMilestone> fetchMilestonesForCourse(String courseId, String fwCode) {
    // NOTE: Model is not completely hydrated as we do not need all fields
    return AJEntityMilestone
        .findBySQL(MILESTONE_LIST_FOR_COURSE, courseId, fwCode);
  }

  public JsonArray fetchMilestoneBYIdForCourse(String courseId, String milestoneId) {
    List<Map> result = Base.findAll(MILESTONE_FETCH_FOR_COURSE, courseId, milestoneId);
    try {
      String resultString = new ObjectMapper().writeValueAsString(result);
      return new JsonArray(resultString);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Not able to serialize values as JSON");
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

  public LazyList<AJEntityMilestone> fetchMilestonesForCourseConsideringRescope(String userId,
      String courseId, String classId, String frameworkCode) {
    List<String> rescopedLessons = fetchRescopedLessons(userId, courseId, classId);
    if (rescopedLessons.isEmpty()) {
      return fetchMilestonesForCourse(courseId, frameworkCode);
    } else {
      return AJEntityMilestone
          .findBySQL(MILESTONE_LIST_FOR_COURSE_EXCLUDING_SPECIFIED_LESSONS, courseId, frameworkCode,
              DbHelperUtil.toPostgresArrayString(rescopedLessons));
    }
  }

  private List<String> fetchRescopedLessons(String userId, String courseId, String classId) {
    Object userRescopedContent = Base
        .firstCell(FETCH_USER_RESCOPED_CONTENT, userId, courseId, classId);
    if (userRescopedContent != null) {
      JsonObject rescopedContents = new JsonObject(userRescopedContent.toString());
      if (!rescopedContents.isEmpty()) {
        JsonArray lessons = rescopedContents.getJsonArray("lessons");
        if (lessons != null && !lessons.isEmpty()) {
          List<String> lessonsList = new ArrayList<>(lessons.size());
          for (Object lesson : lessons) {
            lessonsList.add(lesson.toString());
          }
          return lessonsList;
        } else {
          return Collections.emptyList();
        }
      } else {
        return Collections.emptyList();
      }
    } else {
      return Collections.emptyList();
    }
  }

}
