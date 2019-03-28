package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.exceptionhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author renuka
 * 
 */
public final class ExceptionHandlerRegistry {

  private static final List<ExceptionHandler> EXCEPTION_HANDLERS;

  private ExceptionHandlerRegistry() {}

  public static ExceptionHandlerRegistry getInstance() {
    return Holder.INSTANCE;
  }

  static {
    EXCEPTION_HANDLERS = initializeExceptionHandlers();
  }

  // DefaultExceptionHandler should always be the last one
  private static List<ExceptionHandler> initializeExceptionHandlers() {
    List<ExceptionHandler> exceptionHandlers = new ArrayList<>(2);
    exceptionHandlers.add(UTF8EncodingExceptionHandler.getInstance());
    exceptionHandlers.add(DefaultExceptionHandler.getInstance());
    return Collections.unmodifiableList(exceptionHandlers);
  }

  public List<ExceptionHandler> getHandlers() {
    return EXCEPTION_HANDLERS;
  }

  private static final class Holder {
    private static final ExceptionHandlerRegistry INSTANCE = new ExceptionHandlerRegistry();
  }
}
