package org.gooru.nucleus.handlers.courses.processors.commands;

import java.util.HashMap;
import java.util.Map;

import org.gooru.nucleus.handlers.courses.constants.MessageConstants;
import org.gooru.nucleus.handlers.courses.processors.Processor;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 29/12/16.
 */
public enum CommandProcessorBuilder {

    DEFAULT("default") {
        private final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorBuilder.class);

        @Override
        public Processor build(ProcessorContext context) {
            return () -> {
                LOGGER.error("Invalid operation type passed in, not able to handle");
                throw new InvalidRequestException();
            };
        }
    },
    COURSE_CARDS_GET(MessageConstants.MSG_OP_COURSE_CARDS_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseCardsGetProcessor(context);
        }
    },
    COURSE_GET(MessageConstants.MSG_OP_COURSE_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseGetProcessor(context);
        }
    },
    COURSE_CREATE(MessageConstants.MSG_OP_COURSE_CREATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseCreateProcessor(context);
        }
    },
    COURSE_UPDATE(MessageConstants.MSG_OP_COURSE_UPDATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseUpdateProcessor(context);
        }
    },
    COURSE_DELETE(MessageConstants.MSG_OP_COURSE_DELETE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseDeleteProcessor(context);
        }
    },
    COURSE_COLLABORATOR_UPDATE(MessageConstants.MSG_OP_COURSE_COLLABORATOR_UPDATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseCollaboratorUpdateProcessor(context);
        }
    },
    COURSE_CONTENT_REORDER(MessageConstants.MSG_OP_COURSE_CONTENT_REORDER) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseContentReorderProcessor(context);
        }
    },
    COURSE_MOVE_UNIT(MessageConstants.MSG_OP_COURSE_MOVE_UNIT) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UnitMoveProcessor(context);
        }
    },
    COURSE_REORDER(MessageConstants.MSG_OP_COURSE_REORDER) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CourseReorderProcessor(context);
        }
    },
    COURSE_RESOURCES_GET(MessageConstants.MSG_OP_COURSE_RESOURCES_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new ResourcesByStandardProcessor(context);
        }
    },
    COURSE_ASSESSMENTS_GET(MessageConstants.MSG_OP_COURSE_ASSESSMENTS_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new AssessmentsByCourseProcessor(context);
        }
    },
    COURSE_COLLECTIONS_GET(MessageConstants.MSG_OP_COURSE_COLLECTIONS_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CollectionsByCourseProcessor(context);
        }
    },
    UNIT_GET(MessageConstants.MSG_OP_UNIT_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UnitGetProcessor(context);
        }
    },
    UNIT_CREATE(MessageConstants.MSG_OP_UNIT_CREATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UnitCreateProcessor(context);
        }
    },
    UNIT_UPDATE(MessageConstants.MSG_OP_UNIT_UPDATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UnitUpdateProcessor(context);
        }
    },
    UNIT_DELETE(MessageConstants.MSG_OP_UNIT_DELETE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UnitDeleteProcessor(context);
        }
    },
    UNIT_CONTENT_REORDER(MessageConstants.MSG_OP_UNIT_CONTENT_REORDER) {
        @Override
        public Processor build(ProcessorContext context) {
            return new UnitContentReorderProcessor(context);
        }
    },
    UNIT_MOVE_LESSON(MessageConstants.MSG_OP_UNIT_MOVE_LESSON) {
        @Override
        public Processor build(ProcessorContext context) {
            return new LessonMoveProcessor(context);
        }
    },
    LESSON_GET(MessageConstants.MSG_OP_LESSON_GET) {
        @Override
        public Processor build(ProcessorContext context) {
            return new LessonGetProcessor(context);
        }
    },
    LESSON_CREATE(MessageConstants.MSG_OP_LESSON_CREATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new LessonCreateProcessor(context);
        }
    },
    LESSON_UPDATE(MessageConstants.MSG_OP_LESSON_UPDATE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new LessonUpdateProcessor(context);
        }
    },
    LESSON_DELETE(MessageConstants.MSG_OP_LESSON_DELETE) {
        @Override
        public Processor build(ProcessorContext context) {
            return new LessonDeleteProcessor(context);
        }
    },
    LESSON_CONTENT_REORDER(MessageConstants.MSG_OP_LESSON_CONTENT_REORDER) {
        @Override
        public Processor build(ProcessorContext context) {
            return new LessonContentReorderProcessor(context);
        }
    },
    LESSON_MOVE_COLLECTION(MessageConstants.MSG_OP_LESSON_MOVE_COLLECTION) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CollectionMoveProcessor(context);
        }
    },
    LESSON_REMOVE_COLLECTION(MessageConstants.MSG_OP_LESSON_REMOVE_COLLECTION) {
        @Override
        public Processor build(ProcessorContext context) {
            return new CollectionRemoveProcessor(context);
        }
    };

    private String name;

    CommandProcessorBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    private static final Map<String, CommandProcessorBuilder> LOOKUP = new HashMap<>();

    static {
        for (CommandProcessorBuilder builder : values()) {
            LOOKUP.put(builder.getName(), builder);
        }
    }

    public static CommandProcessorBuilder lookupBuilder(String name) {
        CommandProcessorBuilder builder = LOOKUP.get(name);
        if (builder == null) {
            return DEFAULT;
        }
        return builder;
    }

    public abstract Processor build(ProcessorContext context);
}
