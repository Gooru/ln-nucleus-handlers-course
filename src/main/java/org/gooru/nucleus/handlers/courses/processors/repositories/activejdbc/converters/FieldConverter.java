package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.converters;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbutils.DbHelperUtil;
import org.postgresql.util.PGobject;
import io.vertx.core.json.JsonArray;

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

  static PGobject convertFieldJsonArrayToTextArray(Object value) {
    if (value != null) {
      JsonArray valueJsonArray = (JsonArray) value;
      return convertFieldToTextArray(valueJsonArray.getList());
    }
    return null;
  }

  static PGobject convertFieldToTextArray(Object value) {
    String TEXT_ARRAY_TYPE = "text[]";
    PGobject pgObject = new PGobject();
    pgObject.setType(TEXT_ARRAY_TYPE);
    try {
      pgObject.setValue(
          value == null ? null : DbHelperUtil.toPostgresArrayString((List<String>) value));
      return pgObject;
    } catch (SQLException e) {
      return null;
    }
  }

  PGobject convertField(Object fieldValue);
}
