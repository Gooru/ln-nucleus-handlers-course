package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.UUID;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author ashish.
 */

@Table("milestone_lesson_map")
@IdName("id")
public class AJEntityMilestone extends Model {

  private static final String ID = "id";
  private static final String MILESTONE_ID = "milestone_id";
  private static final String COURSE_ID = "course_id";
  private static final String UNIT_ID = "unit_id";
  private static final String LESSON_ID = "lesson_id";
  private static final String GRADE_ID = "grade_id";
  private static final String GRADE_SEQ = "grade_seq";
  private static final String GRADE_NAME = "grade_name";
  private static final String FW_CODE = "fw_code";
  private static final String TX_SUBJECT_CODE = "tx_subject_code";
  private static final String TX_DOMAIN_ID = "tx_domain_id";
  private static final String TX_DOMAIN_CODE = "tx_domain_code";
  private static final String TX_DOMAIN_SEQ = "tx_domain_seq";
  private static final String TX_COMP_CODE = "tx_comp_code";
  private static final String TX_COMP_NAME = "tx_comp_name";
  private static final String TX_COMP_STUDENT_DESC = "tx_comp_student_desc";
  private static final String TX_COMP_SEQ = "tx_comp_seq";


  public String getMilestoneId() {
    return this.getString(MILESTONE_ID);
  }

  public UUID getCourseId() {
    return UUID.fromString(this.getString(COURSE_ID));
  }

  public UUID getUnitId() {
    return UUID.fromString(this.getString(UNIT_ID));
  }

  public UUID getLessonId() {
    return UUID.fromString(this.getString(LESSON_ID));
  }

  public Long getGradeId() {
    return this.getLong(GRADE_ID);
  }

  public String getGradeName() {
    return this.getString(GRADE_NAME);
  }

  public int getGradeSeq() {
    return this.getInteger(GRADE_SEQ);
  }

  public String getFwCode() {
    return this.getString(FW_CODE);
  }

  public String getSubjectCode() {
    return this.getString(TX_SUBJECT_CODE);
  }

  public Long getDomainId() {
    return this.getLong(TX_DOMAIN_ID);
  }

  public String getDomainCode() {
    return this.getString(TX_DOMAIN_CODE);
  }

  public Integer getDomainSeq() {
    return this.getInteger(TX_DOMAIN_SEQ);
  }

  public String getCompCode() {
    return this.getString(TX_COMP_CODE);
  }

  public String getCompName() {
    return this.getString(TX_COMP_NAME);
  }

  public String getCompStudentDesc() {
    return this.getString(TX_COMP_STUDENT_DESC);
  }

  public Integer getCompSeq() {
    return this.getInteger(TX_COMP_SEQ);
  }

}
