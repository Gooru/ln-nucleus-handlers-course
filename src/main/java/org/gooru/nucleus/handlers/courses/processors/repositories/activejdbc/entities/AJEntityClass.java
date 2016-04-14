package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table("class")
@IdName("id")
public class AJEntityClass extends Model {

    public static final int CURRENT_VERSION = 3;
}
