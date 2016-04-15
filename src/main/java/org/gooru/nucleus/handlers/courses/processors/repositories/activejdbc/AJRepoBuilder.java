package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseCollaboratorRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.LessonRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.UnitRepo;

public class AJRepoBuilder {

    public CourseRepo buildCourseRepo(ProcessorContext context) {
        return new AJCourseRepo(context);
    }

    public CourseCollaboratorRepo buildCourseCollaboratorRepo(ProcessorContext context) {
        return new AJCourseCollaboratorRepo(context);
    }

    public UnitRepo buildUnitRepo(ProcessorContext context) {
        return new AJUnitRepo(context);
    }

    public LessonRepo buildLessonRepo(ProcessorContext context) {
        return new AJLessonRepo(context);
    }
}
