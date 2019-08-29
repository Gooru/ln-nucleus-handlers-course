package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;


class LessonPlanDeleteProcessor extends AbstractCommandProcessor {


  public LessonPlanDeleteProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {}

  @Override
  protected MessageResponse processCommand() {
    return new RepoBuilder().buildLessonPlanRepo(context).deleteLessonPlan();

  }
}
