package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.CommonConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Model;
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
    
    public static JsonObject calculateTagDifference(ProcessorContext context, Model entity) {
        JsonObject result = new JsonObject();
        String existingTagsAsString = entity.getString(CommonConstants.TAXONOMY);
        JsonObject existingTags = existingTagsAsString != null && !existingTagsAsString.isEmpty()
            ? new JsonObject(existingTagsAsString) : new JsonObject();
        JsonObject newTags = context.request().getJsonObject(CommonConstants.TAXONOMY);

        if (existingTags.isEmpty() && newTags != null && !newTags.isEmpty()) {
            result.put(CommonConstants.TAGS_ADDED, newTags.copy());
            result.put(CommonConstants.TAGS_REMOVED, new JsonObject());
        } else if (!existingTags.isEmpty() && (newTags == null || newTags.isEmpty())) {
            result.put(CommonConstants.TAGS_ADDED, new JsonObject());
            result.put(CommonConstants.TAGS_REMOVED, existingTags.copy());
        } else if (!existingTags.isEmpty() && newTags != null && !newTags.isEmpty()) {
            JsonObject toBeAdded = new JsonObject();
            JsonObject toBeRemoved = existingTags.copy();
            newTags.forEach(entry -> {
                String key = entry.getKey();
                if (toBeRemoved.containsKey(key)) {
                    toBeRemoved.remove(key);
                } else {
                    toBeAdded.put(key, entry.getValue());
                }
            });

            if (toBeAdded.isEmpty() && toBeRemoved.isEmpty()) {
                return null;
            }

            result.put(CommonConstants.TAGS_ADDED, toBeAdded);
            result.put(CommonConstants.TAGS_REMOVED, toBeRemoved);
        }

        return result;
    }
    
    public static JsonObject generateTagsToDelete(Model entity) {
        JsonObject result = new JsonObject();
        String existingTagsAsString = entity.getString(CommonConstants.TAXONOMY);
        JsonObject existingTags = (existingTagsAsString != null && !existingTagsAsString.isEmpty())
            ? new JsonObject(existingTagsAsString) : null;

        return existingTags != null ? result.put(CommonConstants.TAGS_REMOVED, existingTags) : null;
    }
    
    public static JsonObject generateTagsToAdd(Model entity) {
        JsonObject result = new JsonObject();
        String existingTagsAsString = entity.getString(CommonConstants.TAXONOMY);
        JsonObject existingTags = (existingTagsAsString != null && !existingTagsAsString.isEmpty())
            ? new JsonObject(existingTagsAsString) : null;

        return existingTags != null ? result.put(CommonConstants.TAGS_ADDED, existingTags) : null;
        
    }

    public static String toPostgresArrayString(Collection<String> input) {
        int approxSize = ((input.size() + 1) * 36); // Length of UUID is around 36 chars
        Iterator<String> it = input.iterator();
        if (!it.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder(approxSize);
        sb.append('{');
        for (; ; ) {
            String s = it.next();
            sb.append('"').append(s).append('"');
            if (!it.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',');
        }
    }

}
