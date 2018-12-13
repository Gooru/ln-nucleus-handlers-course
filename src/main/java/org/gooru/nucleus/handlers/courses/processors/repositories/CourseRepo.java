package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public interface CourseRepo {

  MessageResponse fetchCourseCards();

  MessageResponse fetchCourse();

  MessageResponse createCourse();

  MessageResponse updateCourse();

  MessageResponse deleteCourse();

  MessageResponse reorderUnitInCourse();

  MessageResponse moveUnitToCourse();

  MessageResponse reorderCourse();

  MessageResponse fetchResourcesForCourse();

  MessageResponse fetchAssessmentsByCourse();

  MessageResponse fetchCollectionsByCourse();

}
