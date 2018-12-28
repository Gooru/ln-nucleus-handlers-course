package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.validators;

/**
 * Created by ashish on 17/10/16.
 */
public interface ValidatorRegistry {
    FieldValidator lookupValidator(String fieldName);

    default FieldValidator noopSuccessValidator(String fieldName) {
        return (n) -> true;
    }

    default FieldValidator noopFailedValidator(String fieldName) {
        return (n) -> false;
    }
}
