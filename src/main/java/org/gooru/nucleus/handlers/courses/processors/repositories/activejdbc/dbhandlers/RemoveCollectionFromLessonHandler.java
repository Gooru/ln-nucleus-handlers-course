package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.tagaggregator.TagAggregatorRequestBuilderFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveCollectionFromLessonHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RemoveCollectionFromLessonHandler.class);
  private static final String TAGS_REMOVED = "tags_removed";
  private final ProcessorContext context;
  private AJEntityCollection existingCollection;

  public RemoveCollectionFromLessonHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to remove collection");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
          ExecutionStatus.FAILED);
    }

    LOGGER.debug("checkSanity() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse
        .findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE,
            context.courseId(), false);
    if (ajEntityCourse.isEmpty()) {
      LOGGER.warn("course {} not found to remove collection, aborting", context.courseId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    AJEntityCourse course = ajEntityCourse.get(0);
    if (!course.getString(AJEntityCourse.OWNER_ID).equalsIgnoreCase(context.userId())) {
      String strCollaborators = course.getString(AJEntityCourse.COLLABORATOR);
      if (strCollaborators == null || strCollaborators.isEmpty()) {
        LOGGER.warn("user is not owner or collaborator of course to remove collection. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
            ExecutionStatus.FAILED);
      }

      JsonArray collaborators = new JsonArray(strCollaborators);
      if (!collaborators.contains(context.userId())) {
        LOGGER.warn("user is not owner or collaborator of course to remove collection. aborting");
        return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(),
            ExecutionStatus.FAILED);
      }
    }

    LazyList<AJEntityCollection> collections = AJEntityCollection.findBySQL(
        AJEntityCollection.SELECT_COLLECTION_TO_VALIDATE, context.collectionId(),
        context.courseId(),
        context.unitId(), context.lessonId());
    if (collections.isEmpty()) {
      LOGGER.warn("{} collection not found to remove", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(),
          ExecutionStatus.FAILED);
    }

    this.existingCollection = collections.get(0);
    LOGGER.debug("validateRequest() OK");
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    ExecutionResult<MessageResponse> errors = DbHelperUtil.updateCourseTimestamp(context, LOGGER);
    if (errors != null) {
      return errors;
    }
    int updateColCount = AJEntityCollection.update(AJEntityCollection.UPDATE_COLLECTION_REMOVE_CUL,
        AJEntityCollection.UPDATE_COLLECTION_REMOVE_CUL_WHERE, context.collectionId());

    if (updateColCount == 0) {
      LOGGER.warn("Unable to remove collection '{}' from lesson", context.collectionId());
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("unable to remove collection from lesson"),
          ExecutionStatus.FAILED);
    }

    int updateConCount = AJEntityContent.update(AJEntityContent.UPDATE_CONTENT_REMOVE_CULC,
        AJEntityContent.UPDATE_CONTENT_REMOVE_CULC_WHERE, context.courseId(), context.unitId(),
        context.lessonId(), context.collectionId());
    LOGGER.debug("{} contents updated to remove collection {}", updateConCount,
        context.collectionId());

    JsonArray tagsToAggregate = new JsonArray();
    JsonObject tagDiff = calculateTagDifference();
    if (tagDiff != null && !tagDiff.isEmpty()) {
      tagsToAggregate.add(TagAggregatorRequestBuilderFactory
          .getLessonTagAggregatorRequestBuilder(context.lessonId(), tagDiff).build());
    }

    if (tagDiff != null) {
      return new ExecutionResult<>(
          MessageResponseFactory
              .createNoContentResponse(
                  EventBuilderFactory.getRemoveCollectionFromLessonEventBuilder(context.courseId(),
                      context.unitId(), context.lessonId(), context.collectionId()),
                  tagsToAggregate),
          ExecutionStatus.SUCCESSFUL);
    } else {
      return new ExecutionResult<>(
          MessageResponseFactory.createNoContentResponse(
              EventBuilderFactory.getRemoveCollectionFromLessonEventBuilder(context.courseId(),
                  context.unitId(), context.lessonId(), context.collectionId())),
          ExecutionStatus.SUCCESSFUL);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject calculateTagDifference() {
    JsonObject result = new JsonObject();
    String existingTagsAsString = this.existingCollection.getString(AJEntityCollection.TAXONOMY);
    JsonObject existingTags = (existingTagsAsString != null && !existingTagsAsString.isEmpty())
        ? new JsonObject(existingTagsAsString)
        : null;

    return existingTags != null ? result.put(TAGS_REMOVED, existingTags) : null;
  }
}
