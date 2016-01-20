package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.slf4j.Logger;

/**
 * 
 * @author Sachin
 *
 */
public class AJValidatorBuilder {
  
  public CourseValidator buildCourseValidator(ProcessorContext context, Logger logger) {
    return new CourseValidator(context, logger);
  }
  
  public UnitValidator buildUnitValidator(ProcessorContext context, Logger logger) {
    return new UnitValidator(context, logger);
  }

}
