package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.MilestoneRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

public class AJMilestoneRepo implements MilestoneRepo {

  private final ProcessorContext context;

  AJMilestoneRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse fetchCourseWithMilestones() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildCourseFetchWithMilestonesHandler(context));
  }

  @Override
  public MessageResponse fetchMilestone() {
    return new TransactionExecutor()
        .executeTransaction(new DBHandlerBuilder().buildMilestoneFetchHandler(context));
  }
}
