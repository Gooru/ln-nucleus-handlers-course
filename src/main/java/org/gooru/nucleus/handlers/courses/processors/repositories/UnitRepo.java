package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public interface UnitRepo {

  MessageResponse createUnit();

  MessageResponse updateUnit();

  MessageResponse fetchUnit();

  MessageResponse deleteUnit();

  MessageResponse reorderLessonInUnit();
}
