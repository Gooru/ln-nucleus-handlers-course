package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("content")
public class AJEntityContent extends Model {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String IS_DELETED = "is_deleted";
    public static final String CONTENT_FORMAT = "content_format";
    public static final String CONTENT_SUBFORMAT = "content_subformat";
    public static final Object COLLECTION_ID = "collection_id";

    public static final String CONTENT_FORMAT_RESOURCE = "resource";
    public static final String CONTENT_FORMAT_QUESTION = "question";
    public static final String CONTENT_COUNT = "content_count";
    public static final String RESOURCE_COUNT = "resource_count";
    public static final String QUESTION_COUNT = "question_count";

    public static final String SELECT_CONTENT_COUNT_BY_COLLECTION =
        "SELECT count(id) as content_count, content_format, collection_id FROM content WHERE"
            + " collection_id = ANY(?::uuid[]) AND course_id = ?::uuid AND unit_id = ?::uuid AND lesson_id = ?::uuid AND is_deleted = false GROUP BY"
            + " collection_id, content_format";

    public static final List<String> RESOURCES_BY_COURSE_FIELDS = Arrays.asList(ID, TITLE, CONTENT_FORMAT,
            CONTENT_SUBFORMAT);
}
