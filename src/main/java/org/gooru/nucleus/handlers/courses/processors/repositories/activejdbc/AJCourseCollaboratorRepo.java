package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseCollaboratorRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

class AJCourseCollaboratorRepo implements CourseCollaboratorRepo {

  private final ProcessorContext context;

  AJCourseCollaboratorRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse updateCollaborator() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildUpdateCollaboratorHandler(context));
  }

}
