package org.gooru.nucleus.handlers.courses.processors.exceptions;

import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

public class MessageResponseWrapperException extends RuntimeException {

  private final MessageResponse messageResponse;

  public MessageResponseWrapperException(MessageResponse messageResponse) {
    this.messageResponse = messageResponse;
  }

  public MessageResponse getMessageResponse() {
    return messageResponse;
  }
}
