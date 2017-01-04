package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

    private final String userId;
    private final JsonObject session;
    private final JsonObject request;
    private final MultiMap requestHeaders;
    private final String courseId;
    private final String unitId;
    private final String lessonId;
    private final String collectionId;

    public ProcessorContext(String userId, JsonObject session, JsonObject request, String courseId, String unitId,
        String lessonId, String collectionId, MultiMap headers) {
        if (session == null || userId == null || session.isEmpty() || headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Processor Context creation failed because of invalid values");
        }
        this.userId = userId;
        this.session = session.copy();
        this.request = request != null ? request.copy() : null;
        this.requestHeaders = headers;
        this.courseId = courseId;
        this.unitId = unitId;
        this.lessonId = lessonId;
        this.collectionId = collectionId;
    }

    public String userId() {
        return this.userId;
    }

    public JsonObject session() {
        return this.session.copy();
    }

    public JsonObject request() {
        return this.request;
    }

    public String courseId() {
        return this.courseId;
    }

    public String unitId() {
        return this.unitId;
    }

    public MultiMap requestHeaders() {
        return requestHeaders;
    }

    public String lessonId() {

        return this.lessonId;
    }

    public String collectionId() {
        return this.collectionId;
    }

}
