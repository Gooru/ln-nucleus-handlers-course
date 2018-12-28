package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters;

import java.sql.SQLException;

import org.postgresql.util.PGobject;

/**
 * Created by ashish on 28/1/16.
 */
public interface FieldConverter {
    static PGobject convertFieldToJson(Object value) {
        String JSONB_TYPE = "jsonb";
        PGobject pgObject = new PGobject();
        pgObject.setType(JSONB_TYPE);
        try {
            pgObject.setValue(value == null ? null : String.valueOf(value));
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToUuid(String value) {
        String UUID_TYPE = "uuid";
        PGobject pgObject = new PGobject();
        pgObject.setType(UUID_TYPE);
        try {
            pgObject.setValue(value);
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToNamedType(Object value, String type) {
        PGobject pgObject = new PGobject();
        pgObject.setType(type);
        try {
            pgObject.setValue(value == null ? null : String.valueOf(value));
            return pgObject;
        } catch (SQLException e) {
            return null;
        }
    }

    static PGobject convertFieldToIntArray(Object value) {
        String INT_ARRAY_TYPE = "custom-int-array-from-jsonarray";
        PGobject pgObject = new PGobject();
        pgObject.setType(INT_ARRAY_TYPE);
        return pgObject;
    }

    static PGobject convertFieldToTextArray(Object value) {
        String TEXT_ARRAY_TYPE = "custom-text-array-from-jsonarray";
        PGobject pgObject = new PGobject();
        pgObject.setType(TEXT_ARRAY_TYPE);
        return pgObject;
    }

    PGobject convertField(Object fieldValue);
}
