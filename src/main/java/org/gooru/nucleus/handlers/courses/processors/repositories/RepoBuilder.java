package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.AJRepoBuilder;

public class RepoBuilder {

  public CourseRepo buildCourseRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildCourseRepo(context);
  }

  public CourseCollaboratorRepo buildCourseCollaboratorRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildCourseCollaboratorRepo(context);
  }

  public UnitRepo buildUnitRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildUnitRepo(context);
  }

  public LessonRepo buildLessonRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildLessonRepo(context);
  }

  public MilestoneRepo buildMilestoneRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildMilestoneRepo(context);
  }
}
