package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;

/**
 * Created by ashish on 11/1/16.
 */
public class DBHandlerBuilder {

  public DBHandler buildFetchCourseHandler(ProcessorContext context) {
    return new FetchCourseHandler(context);
  }

  public DBHandler buildCreateCourseHandler(ProcessorContext context) {
    return new CreateCourseHandler(context);
  }

  public DBHandler buildUpdateCourseHandler(ProcessorContext context) {
    return new UpdateCourseHandler(context);
  }

  public DBHandler buildDeleteCourseHandler(ProcessorContext context) {
    return new DeleteCourseHandler(context);
  }

  public DBHandler buildCopyCourseHandler(ProcessorContext context) {
    return new CopyCourseHandler(context);
  }

  public DBHandler buildReorderUnitInCourseHandler(ProcessorContext context) {
    return new ReorderUnitInCourseHandler(context);
  }

  public DBHandler buildFetchCollaboratorHandler(ProcessorContext context) {
    return new FetchCollaboratorHandler(context);
  }

  public DBHandler buildUpdateCollaboratorHandler(ProcessorContext context) {
    return new UpdateCollaboratorHandler(context);
  }
}
