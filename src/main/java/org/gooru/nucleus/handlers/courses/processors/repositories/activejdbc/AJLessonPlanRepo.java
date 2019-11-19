package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.LessonPlanRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

class AJLessonPlanRepo implements LessonPlanRepo {

  private final ProcessorContext context;

  AJLessonPlanRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse createLessonPlan() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildCreateLessonPlanHandler(context));
  }

  @Override
  public MessageResponse updateLessonPlan() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildUpdateLessonPlanHandler(context));
  }


  @Override
  public MessageResponse deleteLessonPlan() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildDeleteLessonPlanHandler(context));
  }


}
