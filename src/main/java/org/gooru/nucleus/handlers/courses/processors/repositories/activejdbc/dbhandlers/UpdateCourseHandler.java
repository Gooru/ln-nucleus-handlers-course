package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.courses.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class UpdateCourseHandler implements DBHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCourseHandler.class);
	private final ProcessorContext context;
	private AJEntityCourse courseToUpdate;

	public UpdateCourseHandler(ProcessorContext context) {
		this.context = context;
	}

	@Override
	public ExecutionResult<MessageResponse> checkSanity() {
		if (context.courseId() == null || context.courseId().isEmpty()) {
			LOGGER.warn("invalid course id for update");
			return new ExecutionResult<>(
					MessageResponseFactory.createInvalidRequestResponse("Invalid course id for update"),
					ExecutionStatus.FAILED);
		}

		if (context.request() == null || context.request().isEmpty()) {
			LOGGER.warn("invalid data provided to update course {}", context.courseId());
			return new ExecutionResult<>(
					MessageResponseFactory.createInvalidRequestResponse("Invalid data provided to update course"),
					ExecutionStatus.FAILED);
		}

		if (context.userId() == null || context.userId().isEmpty()
				|| context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
			LOGGER.warn("Anonymous user attempting to update course");
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
		LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE,
				context.courseId(), false, context.userId(), context.userId());
		if (ajEntityCourse.isEmpty()) {
			LOGGER.warn("user is not owner or collaborator of course to update course. aborting");
			return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionStatus.FAILED);
		}

		LOGGER.debug("validateRequest() OK");
		return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
	}

	@Override
	public ExecutionResult<MessageResponse> executeRequest() {
		courseToUpdate = new AJEntityCourse();
		courseToUpdate.setAllFromJson(context.request());
		courseToUpdate.setCourseId(context.courseId());
		courseToUpdate.setModifierId(context.userId());

		Object maxSequenceId = null;
		int sequenceId = 1;
		Object objSubjectBucket = Base.firstCell(AJEntityCourse.SELECT_SUBJECT_BUCKET, context.courseId());
		String subjectBucketFromPaylaod = courseToUpdate.getString(AJEntityCourse.SUBJECT_BUCKET);
		// User is trying to update subject bucket to different subject
		if (subjectBucketFromPaylaod != null && !subjectBucketFromPaylaod.isEmpty()) {
			if (objSubjectBucket != null) {
				String subjectBucket = objSubjectBucket.toString();
				if (!subjectBucketFromPaylaod.equalsIgnoreCase(subjectBucket)) {
					maxSequenceId = Base.firstCell(AJEntityCourse.SELECT_MAX_SEQUENCE_FOR_SUBJECT_BUCKET,
							context.userId(), subjectBucketFromPaylaod);
				}
			} else {
				maxSequenceId = Base.firstCell(AJEntityCourse.SELECT_MAX_SEQUENCE_FOR_SUBJECT_BUCKET, context.userId(),
						subjectBucketFromPaylaod);
			}
		} else {
			// User is trying to update subject bucket to null
			if (objSubjectBucket != null) {
				maxSequenceId = Base.firstCell(AJEntityCourse.SELECT_MAX_SEQUENCE_FOR_NON_SUBJECT_BUCKET,
						context.userId());
			}
		}

		if (maxSequenceId != null) {
			sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;
			courseToUpdate.setInteger(AJEntityCourse.SEQUENCE_ID, sequenceId);
		}

		if (courseToUpdate.hasErrors()) {
			LOGGER.warn("updating course has errors");
			return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
					ExecutionStatus.FAILED);
		}

		if (courseToUpdate.isValid()) {
			if (courseToUpdate.save()) {
				LOGGER.info("course {} updated successfully", context.courseId());
				return new ExecutionResult<>(
						MessageResponseFactory.createNoContentResponse(
								EventBuilderFactory.getUpdateCourseEventBuilder(context.courseId())),
						ExecutionStatus.SUCCESSFUL);
			} else {
				LOGGER.error("error while saving updated course");
				return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
						ExecutionStatus.FAILED);
			}
		} else {
			LOGGER.warn("validation error while updating course");
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
		input.fieldNames().stream().filter(key -> !AJEntityCourse.UPDATABLE_FIELDS.contains(key))
				.forEach(key -> output.put(key, "Field not allowed"));
		return output.isEmpty() ? null : output;
	}

	private JsonObject validateNullFields() {
		JsonObject input = context.request();
		JsonObject output = new JsonObject();
		input.fieldNames().stream()
				.filter(key -> AJEntityCourse.NOTNULL_FIELDS.contains(key)
						&& (input.getValue(key) == null || input.getValue(key).toString().isEmpty()))
				.forEach(key -> output.put(key, "Field should not be empty or null"));
		return output.isEmpty() ? null : output;
	}

	private JsonObject getModelErrors() {
		JsonObject errors = new JsonObject();
		this.courseToUpdate.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
		return errors;
	}
}
