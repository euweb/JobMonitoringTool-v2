package com.company.jobmonitor.service;

import com.company.jobmonitor.entity.ImportedJobExecution;
import org.springframework.context.ApplicationEvent;

/** Event that is fired when a job execution is created or updated */
public class JobExecutionEvent extends ApplicationEvent {

  private final ImportedJobExecution execution;
  private final EventType eventType;

  public enum EventType {
    CREATED,
    UPDATED,
    STATUS_CHANGED
  }

  public JobExecutionEvent(Object source, ImportedJobExecution execution, EventType eventType) {
    super(source);
    this.execution = execution;
    this.eventType = eventType;
  }

  public ImportedJobExecution getExecution() {
    return execution;
  }

  public EventType getEventType() {
    return eventType;
  }
}
