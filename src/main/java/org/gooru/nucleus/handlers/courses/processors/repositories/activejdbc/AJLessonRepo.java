package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.LessonRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public class AJLessonRepo implements LessonRepo {

  private final ProcessorContext context;

  public AJLessonRepo(ProcessorContext context) {
    this.context = context;
  }
  
  @Override
  public MessageResponse createLesson() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCreateLessonHandler(context));
  }

  @Override
  public MessageResponse updateLesson() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateLessonHandler(context));
  }

  @Override
  public MessageResponse fetchLesson() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchLessonHandler(context));
  }

  @Override
  public MessageResponse deleteLesson() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildDeleteLessonHandler(context));
  }

  @Override
  public MessageResponse reorderCollectionsAssessmentsInLesson() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildReorderCollectionsAssessmentsInLessonHandler(context));
  }

  @Override
  public MessageResponse moveCollectionToLesson() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildMoveCollectionToLessonHandler(context));
  }

}
