package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public class AJCourseRepo implements CourseRepo {
  private final ProcessorContext context;

  public AJCourseRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse fetchCourse() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCourseHandler(context));
  }

  @Override
  public MessageResponse createCourse() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCreateCourseHandler(context));
  }

  @Override
  public MessageResponse updateCourse() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateCourseHandler(context));
  }

  @Override
  public MessageResponse deleteCourse() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MessageResponse copyCourse() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MessageResponse reorderUnitInCourse() {
    // TODO Auto-generated method stub
    return null;
  }
}
