package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.formatter;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.ModelDelegate;
import org.javalite.common.Convert;
import org.javalite.common.Escape;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.impl.StringEscapeUtils;

/**
 * Created by ashish on 20/1/16. Simple Json formatter is not aware of any
 * parent child relationship and just assumes that formatter is needed to work
 * on the entity provided. The difference with AJ provided Model.toJson() is
 * difference in handling of "jsonb" field type in postgres
 */
class SimpleJsonFormatter implements JsonFormatter {
    private final String[] attributes;
    private final boolean pretty;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJsonFormatter.class);
    
    public SimpleJsonFormatter(boolean pretty, List<String> attributes) {
        this.pretty = pretty;
        this.attributes = (attributes != null && attributes.size() > 0) ? lowerCased(attributes) : null;
    }

    @Override
    public <T extends Model> String toJson(T model) {
        StringBuilder sb = new StringBuilder();
        String indent = "";
        modelToJson(model, sb, indent);
        return sb.toString();
    }

    @Override
    public <T extends Model> String toJson(LazyList<T> modelList) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (pretty) {
            sb.append('\n');
        }

        for (int i = 0; i < modelList.size(); i++) {
            if (i > 0) {
                sb.append(',');
                if (pretty) {
                    sb.append('\n');
                }
            }
            T model = modelList.get(i);
            modelToJson(model, sb, (pretty ? "  " : ""));
        }
        if (pretty) {
            sb.append('\n');
        }
        sb.append(']');
        return sb.toString();

    }

    private <T extends Model> void modelToJson(T model, StringBuilder sb, String indent) {
        if (pretty) {
            sb.append(indent);
        }
        sb.append('{');
        String[] names;

        if (this.attributes == null) {
            Set<String> attributeNamesAll = ModelDelegate.attributeNames(model.getClass());
            names = lowerCased(attributeNamesAll);
        } else {
            names = this.attributes;
        }
        try {
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                if (pretty) {
                    sb.append("\n  ").append(indent);
                }
                String name = names[i];
                sb.append('"').append(name).append("\":");
                Object v = model.get(name);
                if (v == null) {
                    sb.append("null");
                } else if (v instanceof Number || v instanceof Boolean) {
                    sb.append(v);
                } else if (v instanceof Date) {
                    sb.append('"').append(Convert.toIsoString((Date) v)).append('"');
                } else if (v instanceof PGobject && ((PGobject) v).getType().equalsIgnoreCase("jsonb")) {
                    sb.append(Convert.toString(v));
                } else {
                    sb.append('"');
                    LOGGER.debug("before escaping the data:{}", v);
                    Escape.json(sb, StringEscapeUtils.escapeJava(Convert.toString(v)));
                    LOGGER.debug("after escape: {}", sb.toString());
                    // sb.append(v);
                    sb.append('"');
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pretty) {
            sb.append('\n').append(indent);
        }
        sb.append('}');

        LOGGER.debug("## COMPLETE JSON: {}", sb.toString());
    }

    private String[] lowerCased(Collection<String> collection) {
        String[] array = new String[collection.size()];
        int i = 0;
        for (String elem : collection) {
            array[i++] = elem.toLowerCase();
        }
        return array;
    }
}
