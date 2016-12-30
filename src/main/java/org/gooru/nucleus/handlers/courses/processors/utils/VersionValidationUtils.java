package org.gooru.nucleus.handlers.courses.processors.utils;

import java.util.List;

import org.gooru.nucleus.handlers.courses.processors.exceptions.VersionDeprecatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;

/**
 * @author ashish on 29/12/16.
 */
public final class VersionValidationUtils {

    private static final String API_VERSION = "api.version";
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionValidationUtils.class);

    private VersionValidationUtils() {
        throw new AssertionError();
    }

    public static String validateVersion(List<String> deprecatedVersions, MultiMap messageHeaders) {
        String currentVersion = findVersionFromMessageHeaders(messageHeaders);
        if (currentVersion == null || deprecatedVersions.contains(currentVersion)) {
            LOGGER.error("Version is not supported or null: {}", currentVersion);
            throw new VersionDeprecatedException();
        }
        return currentVersion;
    }

    private static String findVersionFromMessageHeaders(MultiMap messageHeaders) {
        return messageHeaders.get(API_VERSION);
    }
}
