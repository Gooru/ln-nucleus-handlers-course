package org.gooru.nucleus.handlers.courses.constants;

public class MessageConstants {

    public static final String MSG_HEADER_OP = "mb.operation";
    public static final String MSG_HEADER_TOKEN = "session.token";
    public static final String MSG_OP_STATUS = "mb.operation.status";
    public static final String MSG_KEY_SESSION = "session";
    public static final String MSG_OP_STATUS_SUCCESS = "success";
    public static final String MSG_OP_STATUS_ERROR = "error";
    public static final String MSG_OP_STATUS_VALIDATION_ERROR = "error.validation";
    public static final String MSG_USER_ANONYMOUS = "anonymous";
    public static final String MSG_USER_ID = "user_id";
    public static final String MSG_HTTP_STATUS = "http.status";
    public static final String MSG_HTTP_BODY = "http.body";
    public static final String MSG_HTTP_RESPONSE = "http.response";
    public static final String MSG_HTTP_ERROR = "http.error";
    public static final String MSG_HTTP_VALIDATION_ERROR = "http.validation.error";
    public static final String MSG_HTTP_HEADERS = "http.headers";
    public static final String MSG_MESSAGE = "message";

    // Operation names: Also need to be updated in corresponding handlers
    public static final String MSG_OP_COURSE_GET = "course.get";
    public static final String MSG_OP_COURSE_CREATE = "course.create";
    public static final String MSG_OP_COURSE_UPDATE = "course.update";
    public static final String MSG_OP_COURSE_DELETE = "course.delete";
    public static final String MSG_OP_COURSE_COLLABORATOR_UPDATE = "course.collaborator.update";
    public static final String MSG_OP_COURSE_CONTENT_REORDER = "course.content.reorder";
    public static final String MSG_OP_COURSE_MOVE_UNIT = "course.move.unit";
    public static final String MSG_OP_COURSE_REORDER = "course.reorder";
    public static final String MSG_OP_COURSE_RESOURCES_GET = "course.resources.get";
    public static final String MSG_OP_COURSE_ASSESSMENTS_GET = "course.assessments.get";
    public static final String MSG_OP_COURSE_COLLECTIONS_GET = "course.collections.get";
    public static final String MSG_OP_UNIT_GET = "unit.get";
    public static final String MSG_OP_UNIT_CREATE = "unit.create";
    public static final String MSG_OP_UNIT_UPDATE = "unit.update";
    public static final String MSG_OP_UNIT_DELETE = "unit.delete";
    public static final String MSG_OP_UNIT_CONTENT_REORDER = "unit.content.reorder";
    public static final String MSG_OP_UNIT_MOVE_LESSON = "unit.move.lesson";
    public static final String MSG_OP_LESSON_GET = "lesson.get";
    public static final String MSG_OP_LESSON_CREATE = "lesson.create";
    public static final String MSG_OP_LESSON_UPDATE = "lesson.update";
    public static final String MSG_OP_LESSON_DELETE = "lesson.delete";
    public static final String MSG_OP_LESSON_CONTENT_REORDER = "lesson.content.reorder";
    public static final String MSG_OP_LESSON_MOVE_COLLECTION = "lesson.move.collection";
    public static final String MSG_OP_LESSON_REMOVE_COLLECTION = "lesson.remove.collection";

    // Containers for different responses
    public static final String RESP_CONTAINER_MBUS = "mb.container";
    public static final String RESP_CONTAINER_EVENT = "mb.event";

    public static final String COURSE_ID = "courseId";
    public static final String UNIT_ID = "unitId";
    public static final String LESSON_ID = "lessonId";
    public static final String COLLECTION_ID = "collectionId";
    public static final String TAXONOMY = "taxonomy";
    public static final String RESP_JSON_KEY_RESOURCES = "resources";

}
