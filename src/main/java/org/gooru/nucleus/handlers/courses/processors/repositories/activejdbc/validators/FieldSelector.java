package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

import java.util.Set;

/**
 * Created by ashish on 17/10/16.
 */
public interface FieldSelector {
    Set<String> allowedFields();

    default Set<String> mandatoryFields() {
        return null;
    }
}
