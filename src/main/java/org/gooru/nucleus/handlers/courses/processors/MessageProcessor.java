package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidUserException;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
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
      default:
        LOGGER.error("Invalid operation type passed in, not able to handle");
        throw new InvalidRequestException();
      }
      return result;
    } catch (InvalidRequestException e) {
      // TODO: handle exception
    } catch (InvalidUserException e) {
      // TODO: handle exception
    }

    return null;
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
      if(checkCourseId(context)) {
        LOGGER.error("Invalid request, course id not available. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      if(checkRequest(context)) {
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
      if(checkCourseId(context)) {
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
      if(checkCourseId(context)) {
        LOGGER.error("Invalid request, course id not available. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      if(checkRequest(context)) {
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
      if(checkCourseId(context)) {
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
      
      if(checkRequest(context)) {
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
      if(checkCourseId(context)) {
        LOGGER.error("Invalid request, course id not available. Aborting");
        return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
      }
      
      if(checkRequest(context)) {
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
      if(checkRequest(context)) {
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
    LOGGER.debug("received request to get course id : " + courseId);
    return new ProcessorContext(userId, prefs, request, courseId);
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
