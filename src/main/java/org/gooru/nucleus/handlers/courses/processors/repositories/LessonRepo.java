package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public interface LessonRepo {

    MessageResponse createLesson();

    MessageResponse updateLesson();

    MessageResponse fetchLesson();

    MessageResponse deleteLesson();

    MessageResponse reorderCollectionsAssessmentsInLesson();

    MessageResponse moveCollectionToLesson();

    MessageResponse removeCollectionFromLesson();
}
