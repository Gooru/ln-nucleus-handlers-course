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

  public DBHandler buildReorderUnitInCourseHandler(ProcessorContext context) {
    return new ReorderUnitInCourseHandler(context);
  }

  public DBHandler buildUpdateCollaboratorHandler(ProcessorContext context) {
    return new UpdateCollaboratorHandler(context);
  }
  
  //Unit Handlers
  public DBHandler buildCreateUnitHandler(ProcessorContext context) {
    return new CreateUnitHandler(context);
  }

  public DBHandler buildUpdateUnitHandler(ProcessorContext context) {
    return new UpdateUnitHandler(context);
  }

  public DBHandler buildFetchUnitHandler(ProcessorContext context) {
    return new FetchUnitHandler(context);
  }

  public DBHandler buildDeleteUnitHandler(ProcessorContext context) {
    return new DeleteUnitHandler(context);
  }

  public DBHandler buildReorderLessonInUnitHandler(ProcessorContext context) {
    return new ReorderLessonInUnitHandler(context);
  }

  //Lesson Handlers
  public DBHandler buildCreateLessonHandler(ProcessorContext context) {
    return new CreateLessonHandler(context);
  }

  public DBHandler buildUpdateLessonHandler(ProcessorContext context) {
    return new UpdateLessonHandler(context);
  }

  public DBHandler buildFetchLessonHandler(ProcessorContext context) {
    return new FetchLessonHandler(context);
  }

  public DBHandler buildDeleteLessonHandler(ProcessorContext context) {
    return new DeleteLessonHandler(context);
  }

  public DBHandler buildReorderCollectionsAssessmentsInLessonHandler(ProcessorContext context) {
    return new ReorderCollectionsAssessmentsInLessonHandler(context);
  }
}
