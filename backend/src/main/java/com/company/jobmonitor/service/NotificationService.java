package com.company.jobmonitor.service;

import com.company.jobmonitor.entity.ImportedJobExecution;
import com.company.jobmonitor.entity.JobFavorite;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.ImportedJobExecutionRepository;
import com.company.jobmonitor.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Service for sending email notifications about job execution status changes */
@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

  @Autowired private JavaMailSender mailSender;

  @Autowired private UserRepository userRepository;

  @Autowired private ImportedJobExecutionRepository executionRepository;

  @Value("${app.notification.from-email:noreply@jobmonitor.com}")
  private String fromEmail;

  @Value("${app.notification.enabled:true}")
  private boolean notificationsEnabled;

  @Value("${app.notification.subject-prefix:[Job Monitor]}")
  private String subjectPrefix;

  @Value("${app.notification.mock-mode:false}")
  private boolean mockMode;

  /** Send notification for job failure */
  public void sendJobFailureNotification(JobFavorite favorite, ImportedJobExecution execution) {
    if (!notificationsEnabled) {
      logger.debug("Notifications are disabled");
      return;
    }

    try {
      User user = userRepository.findById(favorite.getUserId()).orElse(null);
      if (user == null || user.getEmail() == null) {
        logger.warn(
            "No user or email found for favorite job notification: {}", favorite.getJobName());
        return;
      }

      String subject = String.format("%s Job Failed: %s", subjectPrefix, favorite.getJobName());
      String body = buildFailureNotificationBody(execution);

      sendEmail(user.getEmail(), subject, body);

      logger.info(
          "Sent failure notification for job '{}' to {}", favorite.getJobName(), user.getEmail());

    } catch (Exception e) {
      logger.error("Failed to send failure notification for job '{}'", favorite.getJobName(), e);
    }
  }

  /** Send notification for job success */
  public void sendJobSuccessNotification(JobFavorite favorite, ImportedJobExecution execution) {
    if (!notificationsEnabled) {
      return;
    }

    try {
      User user = userRepository.findById(favorite.getUserId()).orElse(null);
      if (user == null || user.getEmail() == null) {
        logger.warn(
            "No user or email found for favorite job notification: {}", favorite.getJobName());
        return;
      }

      String subject =
          String.format("%s Job Completed Successfully: %s", subjectPrefix, favorite.getJobName());
      String body = buildSuccessNotificationBody(execution);

      sendEmail(user.getEmail(), subject, body);

      logger.info(
          "Sent success notification for job '{}' to {}", favorite.getJobName(), user.getEmail());

    } catch (Exception e) {
      logger.error("Failed to send success notification for job '{}'", favorite.getJobName(), e);
    }
  }

  /** Send notification for job start */
  public void sendJobStartNotification(JobFavorite favorite, ImportedJobExecution execution) {
    if (!notificationsEnabled) {
      return;
    }

    try {
      User user = userRepository.findById(favorite.getUserId()).orElse(null);
      if (user == null || user.getEmail() == null) {
        logger.warn(
            "No user or email found for favorite job notification: {}", favorite.getJobName());
        return;
      }

      String subject = String.format("%s Job Started: %s", subjectPrefix, favorite.getJobName());
      String body = buildStartNotificationBody(execution);

      sendEmail(user.getEmail(), subject, body);

      logger.info(
          "Sent start notification for job '{}' to {}", favorite.getJobName(), user.getEmail());

    } catch (Exception e) {
      logger.error("Failed to send start notification for job '{}'", favorite.getJobName(), e);
    }
  }

  /** Check for recently failed executions and send notifications if needed */
  public void checkAndNotifyJobFailure(JobFavorite favorite) {
    try {
      // Get recent executions for this job (last 5 minutes)
      LocalDateTime since = LocalDateTime.now().minusMinutes(5);
      List<ImportedJobExecution> recentExecutions =
          executionRepository.findByJobNameAndStatusAndImportTimestampAfter(
              favorite.getJobName(), "FAIL", since);

      for (ImportedJobExecution execution : recentExecutions) {
        sendJobFailureNotification(favorite, execution);
      }

    } catch (Exception e) {
      logger.error("Error checking for failed executions for job '{}'", favorite.getJobName(), e);
    }
  }

  /** Send email using JavaMailSender */
  private void sendEmail(String to, String subject, String body) {
    if (mockMode) {
      // Mock mode for development - log emails instead of sending
      logger.info(
          "\n"
              + "=== MOCK EMAIL ===\n"
              + "To: {}\n"
              + "Subject: {}\n"
              + "Body:\n{}\n"
              + "==================",
          to,
          subject,
          body);
      return;
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);

      logger.debug("Email sent to {} with subject: {}", to, subject);

    } catch (Exception e) {
      logger.error("Failed to send email to {} with subject: {}", to, subject, e);
      // In development, don't fail the operation if email fails
      if (!mockMode) {
        throw e;
      }
    }
  }

  /** Build email body for failure notifications */
  private String buildFailureNotificationBody(ImportedJobExecution execution) {
    StringBuilder body = new StringBuilder();
    body.append("Job Execution Failed\n");
    body.append("==================\n\n");
    body.append("Job Name: ").append(execution.getJobName()).append("\n");
    body.append("Execution ID: ").append(execution.getExecutionId()).append("\n");
    body.append("Status: ").append(execution.getStatus()).append("\n");

    if (execution.getJobType() != null) {
      body.append("Job Type: ").append(execution.getJobType()).append("\n");
    }

    if (execution.getSubmittedBy() != null) {
      body.append("Submitted By: ").append(execution.getSubmittedBy()).append("\n");
    }

    if (execution.getSubmittedAt() != null) {
      body.append("Submitted At: ").append(formatDateTime(execution.getSubmittedAt())).append("\n");
    }

    if (execution.getStartedAt() != null) {
      body.append("Started At: ").append(formatDateTime(execution.getStartedAt())).append("\n");
    }

    if (execution.getEndedAt() != null) {
      body.append("Ended At: ").append(formatDateTime(execution.getEndedAt())).append("\n");
    }

    if (execution.getDurationSeconds() != null) {
      body.append("Duration: ").append(formatDuration(execution.getDurationSeconds())).append("\n");
    }

    if (execution.getHost() != null) {
      body.append("Host: ").append(execution.getHost()).append("\n");
    }

    body.append("\n");
    body.append("Please check the job execution details for more information.\n");
    body.append("\n");
    body.append("Best regards,\n");
    body.append("Job Monitoring System");

    return body.toString();
  }

  /** Build email body for success notifications */
  private String buildSuccessNotificationBody(ImportedJobExecution execution) {
    StringBuilder body = new StringBuilder();
    body.append("Job Execution Completed Successfully\n");
    body.append("===================================\n\n");
    body.append("Job Name: ").append(execution.getJobName()).append("\n");
    body.append("Execution ID: ").append(execution.getExecutionId()).append("\n");
    body.append("Status: ").append(execution.getStatus()).append("\n");

    if (execution.getStartedAt() != null) {
      body.append("Started At: ").append(formatDateTime(execution.getStartedAt())).append("\n");
    }

    if (execution.getEndedAt() != null) {
      body.append("Completed At: ").append(formatDateTime(execution.getEndedAt())).append("\n");
    }

    if (execution.getDurationSeconds() != null) {
      body.append("Duration: ").append(formatDuration(execution.getDurationSeconds())).append("\n");
    }

    body.append("\n");
    body.append("Best regards,\n");
    body.append("Job Monitoring System");

    return body.toString();
  }

  /** Build email body for start notifications */
  private String buildStartNotificationBody(ImportedJobExecution execution) {
    StringBuilder body = new StringBuilder();
    body.append("Job Execution Started\n");
    body.append("====================\n\n");
    body.append("Job Name: ").append(execution.getJobName()).append("\n");
    body.append("Execution ID: ").append(execution.getExecutionId()).append("\n");
    body.append("Started At: ").append(formatDateTime(execution.getStartedAt())).append("\n");

    if (execution.getSubmittedBy() != null) {
      body.append("Submitted By: ").append(execution.getSubmittedBy()).append("\n");
    }

    body.append("\n");
    body.append("Best regards,\n");
    body.append("Job Monitoring System");

    return body.toString();
  }

  /** Format LocalDateTime for display */
  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
  }

  /** Format duration in seconds to readable format */
  private String formatDuration(Long durationSeconds) {
    if (durationSeconds == null) {
      return "N/A";
    }

    long hours = durationSeconds / 3600;
    long minutes = (durationSeconds % 3600) / 60;
    long seconds = durationSeconds % 60;

    if (hours > 0) {
      return String.format("%dh %dm %ds", hours, minutes, seconds);
    } else if (minutes > 0) {
      return String.format("%dm %ds", minutes, seconds);
    } else {
      return String.format("%ds", seconds);
    }
  }

  /** Test email configuration by sending a test message */
  public void sendTestNotification(String emailAddress) {
    if (!notificationsEnabled) {
      throw new RuntimeException("Notifications are disabled");
    }

    String subject = subjectPrefix + " Test Notification";
    String body =
        "This is a test notification from the Job Monitoring System.\n\n"
            + "If you receive this email, the notification system is working correctly.\n\n"
            + "Timestamp: "
            + formatDateTime(LocalDateTime.now())
            + "\n\n"
            + "Best regards,\nJob Monitoring System";

    sendEmail(emailAddress, subject, body);
    logger.info("Sent test notification to {}", emailAddress);
  }
}
