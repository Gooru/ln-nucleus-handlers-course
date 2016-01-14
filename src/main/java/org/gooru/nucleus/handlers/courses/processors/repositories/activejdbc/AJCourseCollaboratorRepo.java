package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseCollaboratorRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public class AJCourseCollaboratorRepo implements CourseCollaboratorRepo {
  private final ProcessorContext context;

  public AJCourseCollaboratorRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse fetchCollaborator() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCollaboratorHandler(context));
  }

  @Override
  public MessageResponse updateCollaborator() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateCollaboratorHandler(context));
  }

}
