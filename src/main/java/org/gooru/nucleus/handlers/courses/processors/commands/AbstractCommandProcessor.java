package org.gooru.nucleus.handlers.courses.processors.commands;

import java.util.ArrayList;
import java.util.List;
import org.gooru.nucleus.handlers.courses.processors.Processor;
import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.courses.processors.utils.VersionValidationUtils;

/**
 * @author ashish on 29/12/16.
 */
abstract class AbstractCommandProcessor implements Processor {

  protected List<String> deprecatedVersions = new ArrayList<>();
  protected final ProcessorContext context;
  protected String version;

  protected AbstractCommandProcessor(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse process() {
    setDeprecatedVersions();
    version = VersionValidationUtils.validateVersion(deprecatedVersions, context.requestHeaders());
    return processCommand();
  }

  protected abstract void setDeprecatedVersions();

  protected abstract MessageResponse processCommand();
}
