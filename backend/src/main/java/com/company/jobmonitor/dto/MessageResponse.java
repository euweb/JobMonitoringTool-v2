package com.company.jobmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for simple message responses.
 *
 * <p>Used for API endpoints that need to return a simple text message, such as success
 * confirmations, error messages, or status updates.
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Simple message response for API operations")
public class MessageResponse {

  @Schema(description = "Response message", example = "User created successfully")
  private String message;

  public MessageResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
