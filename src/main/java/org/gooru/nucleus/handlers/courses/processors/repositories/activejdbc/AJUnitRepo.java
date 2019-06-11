package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.UnitRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

class AJUnitRepo implements UnitRepo {

  private final ProcessorContext context;

  AJUnitRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse createUnit() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildCreateUnitHandler(context));
  }

  @Override
  public MessageResponse updateUnit() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildUpdateUnitHandler(context));
  }

  @Override
  public MessageResponse fetchUnit() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildFetchUnitHandler(context));
  }

  @Override
  public MessageResponse deleteUnit() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildDeleteUnitHandler(context));
  }

  @Override
  public MessageResponse reorderLessonInUnit() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildReorderLessonInUnitHandler(context));
  }

  @Override
  public MessageResponse moveLessonToUnit() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildMoveLessonToUnitHandler(context));
  }

}
