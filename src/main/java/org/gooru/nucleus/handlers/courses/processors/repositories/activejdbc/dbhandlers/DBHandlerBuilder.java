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
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
  
  public DBHandler buildCopyCourseHandler(ProcessorContext context) {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
  
  public DBHandler buildReorderUnitInCourseHandler(ProcessorContext context) {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
  
  public DBHandler buildFetchCollaboratorHandler(ProcessorContext context) {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
  
  public DBHandler buildUpdateCollaboratorHandler(ProcessorContext context) {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
}
