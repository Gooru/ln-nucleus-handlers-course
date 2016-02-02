package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("content")
public class AJEntityContent extends Model {

  public static final String ID = "id";
  public static final String IS_DELETED = "is_deleted";
}
