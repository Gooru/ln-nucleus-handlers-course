package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.eventbus.Message;

public class ProcessorBuilder {
  
  private Message<Object> message;

  public ProcessorBuilder(Message<Object> message) {
    this.message = message;
  }
  
  public Processor build() {
    return new MessageProcessor(message);
  }
}
