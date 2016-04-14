package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter;

import java.util.List;

/**
 * Created by ashish on 20/1/16.
 */
public class JsonFormatterBuilder {

    public JsonFormatter buildSimpleJsonFormatter(boolean pretty, List<String> attributes) {

        return new SimpleJsonFormatter(pretty, attributes);
    }
}
