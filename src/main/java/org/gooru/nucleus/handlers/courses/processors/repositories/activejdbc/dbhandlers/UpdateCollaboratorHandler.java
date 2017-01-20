package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UpdateCollaboratorHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollaboratorHandler.class);
    private final ProcessorContext context;
    private AJEntityCourse courseUpdateCollab;
    private AJEntityCourse course;
    private static final String COLLABORATORS_REMOVED = "collaborators.removed";
    private static final String COLLABORATORS_ADDED = "collaborators.added";
    private JsonObject diffCollaborators;

    public UpdateCollaboratorHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {

        if (context.courseId() == null || context.courseId().isEmpty()) {
            LOGGER.warn("invalid course id to update collaborator");
            return new ExecutionResult<>(MessageResponseFactory
                .createInvalidRequestResponse("Invalid course id provided to update collaborator"),
                ExecutionStatus.FAILED);
        }

        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("invalid request received, aborting");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse("Invalid data provided request"),
                ExecutionStatus.FAILED);
        }

        if (context.userId() == null || context.userId().isEmpty() || context.userId()
            .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to update course collaborator");
            return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
        }

        JsonObject validateErrors = validateFields();
        if (validateErrors != null && !validateErrors.isEmpty()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(validateErrors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        JsonObject notNullErrors = validateNullFields();
        if (notNullErrors != null && !notNullErrors.isEmpty()) {
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(notNullErrors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        LOGGER.debug("checkSanity() OK");
        return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityCourse> courses =
            AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_VALIDATE, context.courseId(), false);

        if (courses.size() > 1) {
            this.course = courses.get(0);
            // check whether user is owner of course
            if (!context.userId().equalsIgnoreCase(this.course.getOwnerId())) {
                LOGGER.warn("user is not owner of course to update collaborator. aborting");
                return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
            }
        } else {
            LOGGER.warn("course {} not found to update collaborators, aborting", context.courseId());
            return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(), ExecutionStatus.FAILED);
        }
        diffCollaborators = calculateDiffOfCollaborators();

        return AuthorizerBuilder
            .buildTenantCollaboratorAuthorizer(context, diffCollaborators.getJsonArray(COLLABORATORS_ADDED))
            .authorize(course);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        courseUpdateCollab = new AJEntityCourse();
        courseUpdateCollab.setCourseId(context.courseId());
        courseUpdateCollab.setModifierId(context.userId());
        courseUpdateCollab.setCollaborator(context.request().getJsonArray(AJEntityCourse.COLLABORATOR).toString());

        if (courseUpdateCollab.hasErrors()) {
            LOGGER.warn("updating course collaborator has errors");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }

        if (courseUpdateCollab.save()) {
            LOGGER.info("updated collaborators of course {} successfully", context.courseId());
            return new ExecutionResult<>(MessageResponseFactory.createNoContentResponse(
                EventBuilderFactory.getUpdateCourseCollaboratorEventBuilder(context.courseId(), diffCollaborators)),
                ExecutionStatus.SUCCESSFUL);
        } else {
            LOGGER.error("error while updating course collaborator");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionStatus.FAILED);
        }
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private JsonObject validateFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();
        input.fieldNames().stream().filter(key -> !AJEntityCourse.COLLABORATOR_FIELD.contains(key))
            .forEach(key -> output.put(key, "Field not allowed"));
        return output.isEmpty() ? null : output;
    }

    private JsonObject validateNullFields() {
        JsonObject input = context.request();
        JsonObject output = new JsonObject();
        input.fieldNames().stream().filter(
            key -> AJEntityCourse.COLLABORATOR_FIELD.contains(key) && (input.getValue(key) == null || input
                .getValue(key).toString().isEmpty()))
            .forEach(key -> output.put(key, "Field should not be empty or null"));
        return output.isEmpty() ? null : output;
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        this.courseUpdateCollab.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }

    private JsonObject calculateDiffOfCollaborators() {
        JsonObject result = new JsonObject();
        // Find current collaborators
        String currentCollaboratorsAsString = this.course.getString(AJEntityCourse.COLLABORATOR);
        JsonArray currentCollaborators;
        currentCollaborators = currentCollaboratorsAsString != null && !currentCollaboratorsAsString.isEmpty() ?
            new JsonArray(currentCollaboratorsAsString) : new JsonArray();
        JsonArray newCollaborators = this.context.request().getJsonArray(AJEntityCourse.COLLABORATOR);
        if (currentCollaborators.isEmpty() && !newCollaborators.isEmpty()) {
            // Adding all
            result.put(COLLABORATORS_ADDED, newCollaborators.copy());
            result.put(COLLABORATORS_REMOVED, new JsonArray());
        } else if (!currentCollaborators.isEmpty() && newCollaborators.isEmpty()) {
            // Removing all
            result.put(COLLABORATORS_ADDED, new JsonArray());
            result.put(COLLABORATORS_REMOVED, currentCollaborators.copy());
        } else if (!currentCollaborators.isEmpty() && !newCollaborators.isEmpty()) {
            // Do the diffing
            JsonArray toBeAdded = new JsonArray();
            JsonArray toBeDeleted = currentCollaborators.copy();
            for (Object o : newCollaborators) {
                if (toBeDeleted.contains(o)) {
                    toBeDeleted.remove(o);
                } else {
                    toBeAdded.add(o);
                }
            }
            result.put(COLLABORATORS_ADDED, toBeAdded);
            result.put(COLLABORATORS_REMOVED, toBeDeleted);
        } else {
            // WHAT ????
            LOGGER.warn("Updating collaborator with empty payload when current collaborator is empty for course '{}'",
                this.context.courseId());
            result.put(COLLABORATORS_ADDED, new JsonArray());
            result.put(COLLABORATORS_REMOVED, new JsonArray());
        }
        return result;
    }

}
