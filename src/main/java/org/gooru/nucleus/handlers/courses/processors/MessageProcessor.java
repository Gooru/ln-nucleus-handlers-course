package org.gooru.nucleus.handlers.courses.processors;

import java.util.UUID;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

class MessageProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private final Message<Object> message;
    private String userId;
    private JsonObject prefs;
    private JsonObject request;

    public MessageProcessor(Message<Object> message) {
        this.message = message;
    }

    @Override
    public MessageResponse process() {
        MessageResponse result;
        try {
            // Validate the message itself
            ExecutionResult<MessageResponse> validateResult = validateAndInitialize();
            if (validateResult.isCompleted()) {
                return validateResult.result();
            }

            final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
            LOGGER.debug("## Processing Request : {} ##", msgOp);
            switch (msgOp) {
            case MessageConstants.MSG_OP_COURSE_CREATE:
                result = processCourseCreate();
                break;
            case MessageConstants.MSG_OP_COURSE_GET:
                result = processCourseGet();
                break;
            case MessageConstants.MSG_OP_COURSE_UPDATE:
                result = processCourseUpdate();
                break;
            case MessageConstants.MSG_OP_COURSE_DELETE:
                result = processCourseDelete();
                break;
            case MessageConstants.MSG_OP_COURSE_CONTENT_REORDER:
                result = processCourseUnitReorder();
                break;
            case MessageConstants.MSG_OP_COURSE_COLLABORATOR_UPDATE:
                result = processCourseCollaboratorUpdate();
                break;
            case MessageConstants.MSG_OP_COURSE_MOVE_UNIT:
                result = processUnitMove();
                break;
            case MessageConstants.MSG_OP_COURSE_REORDER:
                result = processCourseReorder();
                break;
            case MessageConstants.MSG_OP_COURSE_RESOURCES_GET:
                result = processCourseResourcesGet();
                break;
            case MessageConstants.MSG_OP_UNIT_CREATE:
                result = processUnitCreate();
                break;
            case MessageConstants.MSG_OP_UNIT_UPDATE:
                result = processUnitUpdate();
                break;
            case MessageConstants.MSG_OP_UNIT_DELETE:
                result = processUnitDelete();
                break;
            case MessageConstants.MSG_OP_UNIT_GET:
                result = processUnitGet();
                break;
            case MessageConstants.MSG_OP_UNIT_CONTENT_REORDER:
                result = processUnitContentReorder();
                break;
            case MessageConstants.MSG_OP_UNIT_MOVE_LESSON:
                result = processLessonMove();
                break;
            case MessageConstants.MSG_OP_LESSON_CREATE:
                result = processLessonCreate();
                break;
            case MessageConstants.MSG_OP_LESSON_UPDATE:
                result = processLessonUpdate();
                break;
            case MessageConstants.MSG_OP_LESSON_DELETE:
                result = processLessonDelete();
                break;
            case MessageConstants.MSG_OP_LESSON_GET:
                result = processLessonGet();
                break;
            case MessageConstants.MSG_OP_LESSON_CONTENT_REORDER:
                result = processLessonContentReorder();
                break;
            case MessageConstants.MSG_OP_LESSON_MOVE_COLLECTION:
                result = processCollectionMove();
                break;
            default:
                LOGGER.error("Invalid operation type passed in, not able to handle");
                throw new InvalidRequestException();
            }
            return result;
        } catch (InvalidRequestException e) {
            LOGGER.error("Invalid request");
            return MessageResponseFactory.createInternalErrorResponse("Invalid request");
        } catch (InvalidUserException e) {
            LOGGER.error("User is not valid");
            return MessageResponseFactory.createForbiddenResponse();
        } catch (Throwable t) {
            LOGGER.error("Exception while processing request");
            return MessageResponseFactory.createInternalErrorResponse("Something wrong in database transaction");
        }
    }

    private MessageResponse processCourseReorder() {
        ProcessorContext context = createContext();
        return new RepoBuilder().buildCourseRepo(context).reorderCourse();
    }
    
    private MessageResponse processCourseResourcesGet() {
        ProcessorContext context = createContext();
        return new RepoBuilder().buildCourseRepo(context).fetchResourcesForCourse();
    }
    
    private MessageResponse processCollectionMove() {
        ProcessorContext context = createContext();

        if (checkCourseId(context)) {
            LOGGER.error("Course id not available to move collection. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
        }

        if (checkUnitId(context)) {
            LOGGER.error("Unit id not available to move collection. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
        }

        if (checkLessonId(context)) {
            LOGGER.error("Lesson id not available to move collection. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
        }

        return new RepoBuilder().buildLessonRepo(context).moveCollectionToLesson();
    }

    private MessageResponse processLessonMove() {
        ProcessorContext context = createContext();

        if (checkCourseId(context)) {
            LOGGER.error("Course id not available to move lesson. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
        }

        if (checkUnitId(context)) {
            LOGGER.error("Unit id not available to move lesson. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
        }

        return new RepoBuilder().buildUnitRepo(context).moveLessonToUnit();
    }

    private MessageResponse processUnitMove() {
        ProcessorContext context = createContext();

        if (checkCourseId(context)) {
            LOGGER.error("Course id not available to move unit. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
        }

        return new RepoBuilder().buildCourseRepo(context).moveUnitToCourse();
    }

    private MessageResponse processLessonContentReorder() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to reorder lesson content. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to reorder lesson content. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            if (checkLessonId(context)) {
                LOGGER.error("Lesson id not available to reorder lesson content. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
            }

            LOGGER.info("reorder content of lesson {} of unit {}", context.lessonId(), context.unitId());
            return new RepoBuilder().buildLessonRepo(context).reorderCollectionsAssessmentsInLesson();
        } catch (Throwable t) {
            LOGGER.error("Exception while reordering lesson content", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processLessonGet() {
        try {
            ProcessorContext context = createContext();
            if (!checkCourseId(context)) {
                LOGGER.error("Course id not available to get lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (!checkUnitId(context)) {
                LOGGER.error("Unit id not available to get lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            if (!checkLessonId(context)) {
                LOGGER.error("Lesson id not available to get lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
            }

            LOGGER.info("getting lesson {} of unit {}", context.lessonId(), context.unitId());
            return new RepoBuilder().buildLessonRepo(context).fetchLesson();
        } catch (Throwable t) {
            LOGGER.error("Exception while getting lesson", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processLessonDelete() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to delete lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to delete lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            if (checkLessonId(context)) {
                LOGGER.error("Lesson id not available to delete lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
            }

            LOGGER.info("deleting lesson {} of unit {}", context.lessonId(), context.unitId());
            return new RepoBuilder().buildLessonRepo(context).deleteLesson();
        } catch (Throwable t) {
            LOGGER.error("Exception while deleting lesson", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processLessonUpdate() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to update lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to update lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            if (checkLessonId(context)) {
                LOGGER.error("Lesson id not available to update lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
            }

            LOGGER.info("updating lesson {} of unit {}", context.lessonId(), context.unitId());
            return new RepoBuilder().buildLessonRepo(context).updateLesson();
        } catch (Throwable t) {
            LOGGER.error("Exception while updating lesson", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processLessonCreate() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to create lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to create lesson. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            LOGGER.info("creating lesson for unit {} of course {}", context.unitId(), context.courseId());
            return new RepoBuilder().buildLessonRepo(context).createLesson();
        } catch (Throwable t) {
            LOGGER.error("Exception while creating lesson", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processUnitContentReorder() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to reorder lessons. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to reorder lessons. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            LOGGER.info("reordering lessons for unit {} of course {}", context.unitId(), context.courseId());
            return new RepoBuilder().buildUnitRepo(context).reorderLessonInUnit();
        } catch (Throwable t) {
            LOGGER.error("Exception while reordering lessons", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processUnitGet() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to get unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to get unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            LOGGER.info("getting unit {} of course {}", context.unitId(), context.courseId());
            return new RepoBuilder().buildUnitRepo(context).fetchUnit();
        } catch (Throwable t) {
            LOGGER.error("Exception while getting unit", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processUnitDelete() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to delete unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to delete unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            LOGGER.info("deleting unit {} of course {}", context.unitId(), context.courseId());
            return new RepoBuilder().buildUnitRepo(context).deleteUnit();
        } catch (Throwable t) {
            LOGGER.error("Exception while deleting unit", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processUnitUpdate() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to update unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            if (checkUnitId(context)) {
                LOGGER.error("Unit id not available to update unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
            }

            LOGGER.info("updating unit {} of course {}", context.unitId(), context.courseId());
            return new RepoBuilder().buildUnitRepo(context).updateUnit();
        } catch (Throwable t) {
            LOGGER.error("Exception while updating unit", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processUnitCreate() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Course id not available to create unit. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            LOGGER.info("creating new unit for course {}", context.courseId());
            return new RepoBuilder().buildUnitRepo(context).createUnit();
        } catch (Throwable t) {
            LOGGER.error("Exception while creating unit", t);
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private boolean checkLessonId(ProcessorContext context) {
        return validateId(context.lessonId());
    }

    private boolean checkUnitId(ProcessorContext context) {
        return validateId(context.unitId());
    }

    private boolean checkCourseId(ProcessorContext context) {
        return validateId(context.courseId());
    }

    private MessageResponse processCourseCollaboratorUpdate() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Invalid request, course id not available. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            LOGGER.info("updating collaborators for course {}", context.courseId());
            return new RepoBuilder().buildCourseCollaboratorRepo(context).updateCollaborator();
        } catch (Throwable t) {
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processCourseUnitReorder() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Invalid request, course id not available. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            LOGGER.info("reordering units in course {}", context.courseId());
            return new RepoBuilder().buildCourseRepo(context).reorderUnitInCourse();
        } catch (Throwable t) {
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processCourseDelete() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Invalid request, course id not available. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            LOGGER.info("deleting course {}", context.courseId());
            return new RepoBuilder().buildCourseRepo(context).deleteCourse();
        } catch (Throwable t) {
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processCourseUpdate() {
        try {
            ProcessorContext context = createContext();
            if (checkCourseId(context)) {
                LOGGER.error("Invalid request, course id not available. Aborting");
                return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
            }

            LOGGER.info("updating course {}", context.courseId());
            return new RepoBuilder().buildCourseRepo(context).updateCourse();
        } catch (Throwable t) {
            LOGGER.error("Exception while updating course", t.getMessage());
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private MessageResponse processCourseGet() {
        ProcessorContext context = createContext();
        if (checkCourseId(context)) {
            LOGGER.error("Invalid request, course id not available. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
        }

        LOGGER.info("getting course {}", context.courseId());
        return new RepoBuilder().buildCourseRepo(context).fetchCourse();
    }

    private MessageResponse processCourseCreate() {
        try {
            ProcessorContext context = createContext();
            LOGGER.info("Creating new course");
            return new RepoBuilder().buildCourseRepo(context).createCourse();
        } catch (Throwable t) {
            LOGGER.error("Exception while creating course", t.getMessage());
            return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
        }
    }

    private ProcessorContext createContext() {
        String courseId = message.headers().get(MessageConstants.COURSE_ID);
        String unitId = message.headers().get(MessageConstants.UNIT_ID);
        String lessonId = message.headers().get(MessageConstants.LESSON_ID);
        return new ProcessorContext(userId, prefs, request, courseId, unitId, lessonId);
    }

    private ExecutionResult<MessageResponse> validateAndInitialize() {
        if (message == null || !(message.body() instanceof JsonObject)) {
            LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
        if (!validateUser(userId)) {
            LOGGER.error("Invalid user id passed. Not authorized.");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        prefs = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
        request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);

        if (prefs == null || prefs.isEmpty()) {
            LOGGER.error("Invalid preferences obtained, probably not authorized properly");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (request == null) {
            LOGGER.error("Invalid JSON payload on Message Bus");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // All is well, continue processing
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    private boolean validateUser(String userId) {
        return !(userId == null || userId.isEmpty())
            && (userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS) || validateUuid(userId));
    }

    private boolean validateId(String id) {
        return !(id == null || id.isEmpty()) && validateUuid(id);
    }

    private boolean validateUuid(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
