package org.gooru.nucleus.handlers.courses.processors.tagaggregator;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 05-Jan-2018
 */
public final class TagAggregatorRequestBuilderFactory {

    private static final String ENTITY_TYPE = "entity_type";
    private static final String ENTITY_ID = "entity_id";

    private static final String ENTITY_LESSON = "lesson";
    private static final String ENTITY_UNIT = "unit";
    private static final String ENTITY_COURSE = "course";

    private TagAggregatorRequestBuilderFactory() {
        throw new AssertionError();
    }

    public static TagAggregatorRequestBuilder getLessonTagAggregatorRequestBuilder(String lessonId, JsonObject tags) {
        return () -> new JsonObject().put(ENTITY_ID, lessonId).put(ENTITY_TYPE, ENTITY_LESSON).mergeIn(tags);
    }

    public static TagAggregatorRequestBuilder getUnitTagAggregatorRequestBuilder(String unitId, JsonObject tags) {
        return () -> new JsonObject().put(ENTITY_ID, unitId).put(ENTITY_TYPE, ENTITY_UNIT).mergeIn(tags);
    }

    public static TagAggregatorRequestBuilder getCourseTagAggregatorRequestBuilder(String courseId, JsonObject tags) {
        return () -> new JsonObject().put(ENTITY_ID, courseId).put(ENTITY_TYPE, ENTITY_COURSE).mergeIn(tags);
    }
}
