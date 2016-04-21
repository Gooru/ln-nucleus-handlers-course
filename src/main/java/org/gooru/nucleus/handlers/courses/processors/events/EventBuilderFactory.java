package org.gooru.nucleus.handlers.courses.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public class EventBuilderFactory {

    private static final String EVT_COURSE_CREATE = "event.course.create";
    private static final String EVT_COURSE_UPDATE = "event.course.update";
    private static final String EVT_COURSE_DELETE = "event.course.delete";
    private static final String EVT_COURSE_COLLABORATOR_UPDATE = "event.course.collaborator.update";
    private static final String EVT_COURSE_REORDER = "event.course.reorder";
    private static final String EVT_COURSE_CONTENT_REORDER = "event.course.content.reorder";

    private static final String EVT_UNIT_CREATE = "event.unit.create";
    private static final String EVT_UNIT_UPDATE = "event.unit.update";
    private static final String EVT_UNIT_DELETE = "event.unit.delete";
    private static final String EVT_UNIT_CONTENT_REORDER = "event.unit.content.reorder";
    private static final String EVT_UNIT_MOVE = "event.unit.move";

    private static final String EVT_LESSON_CREATE = "event.lesson.create";
    private static final String EVT_LESSON_UPDATE = "event.lesson.update";
    private static final String EVT_LESSON_DELETE = "event.lesson.delete";
    private static final String EVT_LESSON_MOVE = "event.lesson.move";
    private static final String EVT_LESSON_CONTENT_REORDER = "event.lesson.content.reorder";
    private static final String EVT_COLLECTION_MOVE = "event.collection.move";

    private static final String EVENT_NAME = "event.name";
    private static final String EVENT_BODY = "event.body";
    private static final String ID = "id";
    private static final String SUBJECT_BUCKET = "subject_bucket";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";    
    private static final String COURSE_ID = "course_id";
    private static final String UNIT_ID = "unit_id";
    private static final String LESSON_ID = "lesson_id";

    public static EventBuilder getCreateCourseEventBuilder(String courseId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COURSE_CREATE).put(EVENT_BODY,
            new JsonObject().put(ID, courseId));
    }

    public static EventBuilder getCreateLessonEventBuilder(String lessonId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_LESSON_CREATE).put(EVENT_BODY,
            new JsonObject().put(ID, lessonId));
    }

    public static EventBuilder getCreateUnitEventBuilder(String unitId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_UNIT_CREATE).put(EVENT_BODY,
            new JsonObject().put(ID, unitId));
    }

    public static EventBuilder getDeleteCourseEventBuilder(String courseId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COURSE_DELETE).put(EVENT_BODY,
            new JsonObject().put(ID, courseId));
    }

    public static EventBuilder getDeleteLessonEventBuilder(String lessonId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_LESSON_DELETE).put(EVENT_BODY,
            new JsonObject().put(ID, lessonId));
    }

    public static EventBuilder getDeleteUnitEventBuilder(String unitId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_UNIT_DELETE).put(EVENT_BODY,
            new JsonObject().put(ID, unitId));
    }

    public static EventBuilder getMoveCollectionEventBuilder(String courseId, String unitId, String lessonId,
        JsonObject source) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_MOVE).put(EVENT_BODY,
            new JsonObject()
                .put(TARGET, new JsonObject().put(COURSE_ID, courseId).put(UNIT_ID, unitId).put(LESSON_ID, lessonId))
                .put(SOURCE, source));
    }

    public static EventBuilder getMoveLessonEventBuilder(String courseId, String unitId, JsonObject source) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_LESSON_MOVE).put(EVENT_BODY, new JsonObject()
            .put(TARGET, new JsonObject().put(COURSE_ID, courseId).put(UNIT_ID, unitId)).put(SOURCE, source));
    }

    public static EventBuilder getMoveUnitEventBuilder(String courseId, JsonObject source) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_UNIT_MOVE).put(EVENT_BODY,
            new JsonObject().put(TARGET, new JsonObject().put(COURSE_ID, courseId)).put(SOURCE, source));
    }

    public static EventBuilder getReorderCollectionEventBuilder(String lessonId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_LESSON_CONTENT_REORDER).put(EVENT_BODY,
            new JsonObject().put(ID, lessonId));
    }

    public static EventBuilder getReorderLessonEventBuilder(String unitId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_UNIT_CONTENT_REORDER).put(EVENT_BODY,
            new JsonObject().put(ID, unitId));
    }

    public static EventBuilder getReorderUnitEventBuilder(String courseId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COURSE_CONTENT_REORDER).put(EVENT_BODY,
            new JsonObject().put(ID, courseId));
    }

    public static EventBuilder getReorderCourseEventBuilder(String subjectBucket) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COURSE_REORDER).put(EVENT_BODY,
            new JsonObject().put(SUBJECT_BUCKET, subjectBucket));
    }

    public static EventBuilder getUpdateCourseEventBuilder(String courseId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COURSE_UPDATE).put(EVENT_BODY,
            new JsonObject().put(ID, courseId));
    }

    public static EventBuilder getUpdateCourseCollaboratorEventBuilder(String courseId, JsonObject collaborators) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COURSE_COLLABORATOR_UPDATE).put(EVENT_BODY,
            collaborators.put(ID, courseId));
    }

    public static EventBuilder getUpdateLessonEventBuilder(String lessonId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_LESSON_UPDATE).put(EVENT_BODY,
            new JsonObject().put(ID, lessonId));
    }

    public static EventBuilder getUpdateUnitEventBuilder(String unitId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_UNIT_UPDATE).put(EVENT_BODY,
            new JsonObject().put(ID, unitId));
    }
}
