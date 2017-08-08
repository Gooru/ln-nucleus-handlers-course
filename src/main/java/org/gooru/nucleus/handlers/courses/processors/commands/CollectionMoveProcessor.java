package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 29/12/16.
 */
class CollectionMoveProcessor extends AbstractCommandProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionMoveProcessor.class);

    public CollectionMoveProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        // noop
    }

    @Override
    protected MessageResponse processCommand() {

        if (!ValidationUtils.validateId(context.courseId())) {
            LOGGER.error("Course id not available to move collection. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
        }

        if (!ValidationUtils.validateId(context.unitId())) {
            LOGGER.error("Unit id not available to move collection. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid unit id");
        }

        if (!ValidationUtils.validateId(context.lessonId())) {
            LOGGER.error("Lesson id not available to move collection. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid lesson id");
        }

        return new RepoBuilder().buildLessonRepo(context).moveCollectionToLesson();
    }
}
