package org.gooru.nucleus.handlers.courses.app.components;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.bootstrap.startup.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 5/5/16.
 */
public final class AppConfiguration implements Initializer {

  private static final String APP_CONFIG_KEY = "app.configuration";
  private static final String KEY = "__KEY__";
  private static final JsonObject configuration = new JsonObject();
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);
  private static final String COURSE_VERSION_FOR_PREMIUM_CONTENT = "course.version.for.premium.content";

  public static AppConfiguration getInstance() {
    return Holder.INSTANCE;
  }

  private volatile boolean initialized = false;

  private AppConfiguration() {
  }

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    if (!initialized) {
      synchronized (Holder.INSTANCE) {
        if (!initialized) {
          JsonObject appConfiguration = config.getJsonObject(APP_CONFIG_KEY);
          if (appConfiguration == null || appConfiguration.isEmpty()) {
            LOGGER.warn("App configuration is not available");
          } else {
            configuration.put(KEY, appConfiguration);
            initialized = true;
          }
        }
      }
    }
  }

  public String getCourseVersionForPremiumContent() {
    return configuration.getJsonObject(KEY).getString(COURSE_VERSION_FOR_PREMIUM_CONTENT);
  }

  private static final class Holder {

    private static final AppConfiguration INSTANCE = new AppConfiguration();
  }

}
