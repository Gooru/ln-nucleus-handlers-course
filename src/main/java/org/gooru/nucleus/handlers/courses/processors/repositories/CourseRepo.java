package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public interface CourseRepo {

  MessageResponse fetchCourse();

  MessageResponse createCourse();

  MessageResponse updateCourse();

  MessageResponse deleteCourse();

  MessageResponse copyCourse();

  MessageResponse reorderUnitInCourse();
}
