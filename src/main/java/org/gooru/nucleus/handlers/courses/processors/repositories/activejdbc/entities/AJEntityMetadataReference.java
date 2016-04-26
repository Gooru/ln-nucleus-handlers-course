package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table("metadata_reference")
@IdName("id")
public class AJEntityMetadataReference extends Model {

    public static final String ID = "id";
    
    public static final String SELECT_LICENSE = "label = ? AND format = 'license'::metadata_reference_type"; 
    
    public static final String DEFAULT_LICENSE_LABEL = "Public Domain";
}
