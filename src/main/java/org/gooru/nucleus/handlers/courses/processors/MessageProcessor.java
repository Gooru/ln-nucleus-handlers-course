package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        case MessageConstants.MSG_OP_COURSE_COPY:
          result = processCourseCopy();
          break;
        case MessageConstants.MSG_OP_COURSE_DELETE:
          result = processCourseDelete();
          break;
        case MessageConstants.MSG_OP_COURSE_CONTENT_REORDER:
          result = processCourseUnitReorder();
          break;
        case MessageConstants.MSG_OP_COURSE_COLLABORATOR_GET:
          result = processCourseCollaboratorGet();
          break;
        case MessageConstants.MSG_OP_COURSE_COLLABORATOR_UPDATE:
          result = processCourseCollaboratorUpdate();
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
        case MessageConstants.MSG_OP_UNIT_COPY:
          result = processUnitCopy();
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
        case MessageConstants.MSG_OP_LESSON_COPY:
          result = processLessonCopy();
          break;
        default:
          LOGGER.error("Invalid operation type passed in, not able to handle");
          throw new InvalidRequestException();
      }
      return result;
    } catch (InvalidRequestException e) {
      LOGGER.error("Invalid request");
      return MessageResponseFactory.createInternalErrorResponse(e.getMessage());
    } catch (InvalidUserException e) {
      LOGGER.error("User is not valid");
      return MessageResponseFactory.createForbiddenResponse();
    } catch (Throwable t) {
      LOGGER.error("Exception while processing request");
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

  private MessageResponse processLessonCopy() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processLessonContentReorder() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processLessonGet() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processLessonDelete() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processLessonUpdate() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processLessonCreate() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processUnitCopy() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processUnitContentReorder() {
    // TODO Auto-generated method stub
    return null;
  }

  private MessageResponse processUnitGet() {
    try {
      ProcessorContext context = createContext();
      if(checkCourseId(context)) {
        LOGGER.debug("Course id not available to get unit. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      if(checkUnitId(context)) {
        LOGGER.debug("Unit id not available to get unit. Aborting");
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
      if(checkCourseId(context)) {
        LOGGER.debug("Course id not available to delete unit. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      if(checkUnitId(context)) {
        LOGGER.debug("Unit id not available to delete unit. Aborting");
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
      if(checkCourseId(context)) {
        LOGGER.debug("Course id not available to update unit. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      if(checkUnitId(context)) {
        LOGGER.debug("Unit id not available to update unit. Aborting");
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
      if(checkCourseId(context)) {
        LOGGER.debug("Course id not available to create unit. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      LOGGER.info("creating new unit for course {}", context.courseId());
      return new RepoBuilder().buildUnitRepo(context).createUnit();
    } catch (Throwable t) {
      LOGGER.error("Exception while creating unit", t);
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }
  
  private boolean checkUnitId(ProcessorContext context) {
    return (context.unitId() == null || context.unitId().isEmpty());
  }
  
  private boolean checkCourseId(ProcessorContext context) {
    return (context.courseId() == null || context.courseId().isEmpty());
  }

  private boolean checkRequest(ProcessorContext context) {
    return (context.request() == null || context.request().isEmpty());
  }

  private MessageResponse processCourseCollaboratorUpdate() {
    try {
      ProcessorContext context = createContext();
      if (checkCourseId(context)) {
        LOGGER.error("Invalid request, course id not available. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }

      if (checkRequest(context)) {
        LOGGER.error("Invalid input data, Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid input data");
      }

      LOGGER.debug("updating collaborators for course {}", context.courseId());
      return new RepoBuilder().buildCourseCollaboratorRepo(context).updateCollaborator();
    } catch (Throwable t) {
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

  private MessageResponse processCourseCollaboratorGet() {
    try {
      ProcessorContext context = createContext();
      if (checkCourseId(context)) {
        LOGGER.error("Invalid request, course id not available. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }

      LOGGER.debug("getting collaborators for course {}", context.courseId());
      return new RepoBuilder().buildCourseCollaboratorRepo(context).fetchCollaborator();
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

      if (checkRequest(context)) {
        LOGGER.error("Invalid input data, Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid input data");
      }

      LOGGER.debug("reordering units in course {}", context.courseId());
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

      LOGGER.debug("deleting course {}", context.courseId());
      return new RepoBuilder().buildCourseRepo(context).deleteCourse();
    } catch (Throwable t) {
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

  private MessageResponse processCourseCopy() {
    try {
      ProcessorContext context = createContext();

      if (checkRequest(context)) {
        LOGGER.error("Invalid input data, Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid input data");
      }

      LOGGER.debug("copying course");
      return new RepoBuilder().buildCourseRepo(context).copyCourse();
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

      if (checkRequest(context)) {
        LOGGER.error("Invalid input data, Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid input data");
      }
      LOGGER.debug("updating course {}", context.courseId());
      return new RepoBuilder().buildCourseRepo(context).updateCourse();
    } catch (Throwable t) {
      return MessageResponseFactory.createInternalErrorResponse(t.getMessage());
    }
  }

  private MessageResponse processCourseGet() {
    LOGGER.debug("request to get course");
    ProcessorContext context = createContext();
    if (checkCourseId(context)) {
      LOGGER.error("Invalid request, course id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
    }
    LOGGER.debug("received request to get course id : " + context.courseId());
    return new RepoBuilder().buildCourseRepo(context).fetchCourse();
  }

  private MessageResponse processCourseCreate() {
    try {
      LOGGER.debug("creating new course");
      ProcessorContext context = createContext();
      if (checkRequest(context)) {
        LOGGER.error("Invalid input data, Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid input data");
      }

      return new RepoBuilder().buildCourseRepo(context).createCourse();
    } catch (Throwable t) {
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
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
    if (userId == null) {
      LOGGER.error("Invalid user id passed. Not authorized.");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    prefs = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
    request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);

    if (prefs == null || prefs.isEmpty()) {
      LOGGER.error("Invalid preferences obtained, probably not authorized properly");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (request == null) {
      LOGGER.error("Invalid JSON payload on Message Bus");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    // All is well, continue processing
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }
}
