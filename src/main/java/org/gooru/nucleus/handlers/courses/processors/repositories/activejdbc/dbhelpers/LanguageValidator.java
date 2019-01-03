package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhelpers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gooru.nucleus.handlers.courses.app.components.DataSourceRegistry;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public final class LanguageValidator {

  private static Set<Integer> languageIds;
  private static final Logger LOGGER = LoggerFactory.getLogger(LanguageValidator.class);
  private static final String LANG_QUERY = "select id from gooru_language where is_visible = true";

  private LanguageValidator() {}

  public static boolean isValidLanguage(Integer id) {
    return id != null && languageIds.contains(id);
  }

  @SuppressWarnings("rawtypes")
  public static void initialize() {
    try {
      Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
      List result = Base.firstColumn(LANG_QUERY);
      if (result == null || result.isEmpty()) {
        throw new AssertionError("No language values available");
      }
      Set<Integer> foundIds = new HashSet<>(result.size());
      for (Object o : result) {
        foundIds.add((Integer) o);
      }
      languageIds = Collections.unmodifiableSet(foundIds);
    } catch (Throwable e) {
      LOGGER.error("Caught exception while fetching language values", e);
      throw new IllegalStateException(e);
    } finally {
      Base.close();
    }
  }

}