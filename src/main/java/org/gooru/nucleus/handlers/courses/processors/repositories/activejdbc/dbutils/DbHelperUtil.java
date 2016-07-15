package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils;

import java.sql.Timestamp;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;

import io.vertx.core.json.JsonObject;

/**
 * @author ashish on 14/7/16.
 */
public final class DbHelperUtil {

    private DbHelperUtil() {
        throw new AssertionError();
    }

    public static ExecutionResult<MessageResponse> updateCourseTimestamp(ProcessorContext context, Logger LOGGER) {
        AJEntityCourse courseToUpdate = new AJEntityCourse();
        courseToUpdate.setCourseId(context.courseId());
        courseToUpdate.setTimestamp(AJEntityCourse.UPDATED_AT, new Timestamp(System.currentTimeMillis()));
        boolean result = courseToUpdate.save();
        if (!result) {
            LOGGER.error("Course with id '{}' failed to save modified time stamp", context.courseId());
            if (courseToUpdate.hasErrors()) {
                Map<String, String> map = courseToUpdate.errors();
                JsonObject errors = new JsonObject();
                map.forEach(errors::put);
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        return null;
    }

}
