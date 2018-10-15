package org.gooru.nucleus.handlers.courses.processors.commands;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.courses.processors.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author ashish on 29/12/16.
 */
class CourseGetProcessor extends AbstractCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRemoveProcessor.class);

    private JsonObject getCourseData() {
        JsonObject obj = new JsonObject();
        obj.put("title", "Navigate Math (Remix)");
        obj.put("description", "Navigate Math is an outcomes-proven course with hundreds of students across Leadership Public Schools' high school campuses growing nearly 3 years in mathematics in 1 school year as measured by NWEA MAP. Navigate Math is an intervention course and approach that accelerates academic growth and empowers students to direct their own learning. Navigate Math helps students develop the foundational knowledge, skills, and thought processes required to be successful in pre-Algebra and Algebra.");
        obj.put("owner_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("creator_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("original_creator_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("modifier_id", "df956d5f-b7b2-43ae-98a1-c90a12eacaf9");
        obj.put("original_course_id", "7b58ac43-075b-46c4-a7f4-a1ce2b346e85");
        obj.put("publish_status", "unpublished");
        obj.putNull("publish_date");
        obj.put("thumbnail", "a4dfc8ad-eb44-47cf-826b-0d9e213a9f85.png");
        obj.putNull("metadata");
        obj.putNull("taxonomy");
        obj.putNull("collaborator");
        obj.put("visible_on_profile", (Boolean)false);
        obj.put("created_at", "2016-11-07T02:14:38Z");
        obj.put("updated_at", "2018-04-18T08:56:26Z");
        obj.put("sequence_id", 82);
        obj.put("subject_bucket", "CCSS.K12.MA");
        obj.putNull("license");
        obj.putNull("creator_system");
        obj.put("use_case", "Navigate Math is a support math course that accompanies the core math content for grades 7-9 preparing students for the rigor of Algebra by focusing on skill building and changing the way students think about math. Gooru allows us to present a variety of resources and allows students to go at their own pace. Our students are on Gooru for the majority of class. With Gooru and our one-to-one Chromebook program, students have 24-hour access to the content and can choose their own path for learning.");
        obj.put("version", "3.0");
        obj.putNull("aggregated_taxonomy");

        JsonArray unitArr = new JsonArray();
        JsonObject unit = new JsonObject();
        unit.put("unit_id", "1aab410b-4d1d-477f-8218-d06f2b57d03d");
        unit.put("title", "Order of Operations");
        unit.put("sequence_id", 1);
        unit.put("lesson_count", 16);

        unitArr.add(unit);
        unit = new JsonObject();
        unit.put("unit_id", "06a05ffe-d16b-4ad4-8179-bc1ca2348f9d");
        unit.put("title", "Integers & Absolute Value");
        unit.put("sequence_id", 2);
        unit.put("lesson_count", 19);

        unitArr.add(unit);
        unit = new JsonObject();
        unit.put("unit_id", "fda1d831-5cc8-4fd4-8fd6-321b7b299bcf");
        unit.put("title", "Fraction Types");
        unit.put("sequence_id", 3);
        unit.put("lesson_count", 20);

        unitArr.add(unit);
        unit = new JsonObject();
        unit.put("unit_id", "f8886f43-aff0-4180-afe2-105d112f911b");
        unit.put("title", "Fraction Operations");
        unit.put("sequence_id", 4);
        unit.put("lesson_count", 20);

        unitArr.add(unit);

        obj.put("unit_summary", unitArr);

        return obj;
    } 

    public CourseGetProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        // no op
    }

    @Override
    protected MessageResponse processCommand() {
        if (!ValidationUtils.validateId(context.courseId())) {
            LOGGER.error("Invalid request, course id not available. Aborting");
            return MessageResponseFactory.createInvalidRequestResponse("Invalid course id");
        }

        return MessageResponseFactory.createGetResponse(getCourseData());

//        LOGGER.info("getting course {}", context.courseId());
//        return new RepoBuilder().buildCourseRepo(context).fetchCourse();

    }
}
