package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers;

/**
 * This package provides housing of DBHandlers. DBHandler is convenience
 * interface which provides behavior for 1. Non DB validation - Done via
 * DBHandler#checkSanity 2. DB validation - Done via DBHandler#validateRequest
 * 3. DB Operation execution - Done via DBHandler#executeRequest In addition it
 * also provides a way to specify what kind of transaction (readonly or not) is
 * not be used. Based on this, the connection property would be set before the
 * transaction begins.
 * <p>
 * There are also concrete classes for every operation that the repository
 * exposes as DBHandlers implementation. In addition there is a convenience
 * builder for the same
 */