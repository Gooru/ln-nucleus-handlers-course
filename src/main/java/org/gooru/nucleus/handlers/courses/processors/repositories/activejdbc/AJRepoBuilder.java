package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseCollaboratorRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;

public class AJRepoBuilder {

  public CourseRepo buildCourseRepo(ProcessorContext context) {
    return new AJCourseRepo(context);
  }

  public CourseCollaboratorRepo buildCourseCollaboratorRepo(ProcessorContext context) {
    return new AJCourseCollaboratorRepo(context);
  }
}
