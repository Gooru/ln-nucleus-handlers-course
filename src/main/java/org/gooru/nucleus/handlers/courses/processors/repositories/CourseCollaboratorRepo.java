package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public interface CourseCollaboratorRepo {

  MessageResponse fetchCollaborator();

  MessageResponse updateCollaborator();
}
