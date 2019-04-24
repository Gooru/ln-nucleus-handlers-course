package org.gooru.nucleus.handlers.courses.processors.repositories;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

public interface MilestoneRepo {

  MessageResponse fetchCourseWithMilestones();

  MessageResponse fetchMilestone();

}
