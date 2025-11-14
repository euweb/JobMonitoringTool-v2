package com.company.jobmonitor.service;

import com.company.jobmonitor.entity.ImportedJobExecution;
import com.company.jobmonitor.entity.JobFavorite;
import com.company.jobmonitor.repository.JobFavoriteRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Hotfolder service that monitors the import directory for new CSV files and automatically triggers
 * import when new files are detected
 */
@Service
public class HotfolderService {

  private static final Logger logger = LoggerFactory.getLogger(HotfolderService.class);

  @Autowired private CsvImportService csvImportService;

  @Autowired private NotificationService notificationService;

  @Autowired private JobFavoriteRepository favoriteRepository;

  @Value("${app.csv.import.directory:./import}")
  private String importDirectory;

  @Value("${app.hotfolder.enabled:true}")
  private boolean hotfolderEnabled;

  private WatchService watchService;
  private ExecutorService executorService;
  private final AtomicBoolean running = new AtomicBoolean(false);

  /** Initialize the hotfolder monitoring after bean creation */
  @PostConstruct
  public void initialize() {
    if (!hotfolderEnabled) {
      logger.info("Hotfolder monitoring is disabled");
      return;
    }

    try {
      startHotfolderMonitoring();
    } catch (IOException e) {
      logger.error("Failed to initialize hotfolder monitoring", e);
    }
  }

  /** Start monitoring the import directory for new CSV files */
  public void startHotfolderMonitoring() throws IOException {
    if (running.get()) {
      logger.warn("Hotfolder monitoring is already running");
      return;
    }

    // Create import directory if it doesn't exist
    Path importPath = Paths.get(importDirectory);
    if (!Files.exists(importPath)) {
      Files.createDirectories(importPath);
      logger.info("Created import directory: {}", importPath.toAbsolutePath());
    }

    // Initialize WatchService
    watchService = FileSystems.getDefault().newWatchService();
    importPath.register(
        watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

    // Start monitoring in separate thread
    executorService =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread thread = new Thread(r, "Hotfolder-Monitor");
              thread.setDaemon(true);
              return thread;
            });

    running.set(true);
    executorService.submit(this::monitorDirectory);

    logger.info("Started hotfolder monitoring for directory: {}", importPath.toAbsolutePath());
  }

  /** Stop hotfolder monitoring */
  @PreDestroy
  public void stopHotfolderMonitoring() {
    if (!running.get()) {
      return;
    }

    running.set(false);

    if (executorService != null) {
      executorService.shutdown();
    }

    if (watchService != null) {
      try {
        watchService.close();
      } catch (IOException e) {
        logger.error("Error closing watch service", e);
      }
    }

    logger.info("Stopped hotfolder monitoring");
  }

  /** Main monitoring loop */
  private void monitorDirectory() {
    logger.info("Hotfolder monitoring started");

    while (running.get()) {
      try {
        WatchKey key = watchService.take();

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          if (kind == StandardWatchEventKinds.OVERFLOW) {
            continue;
          }

          @SuppressWarnings("unchecked")
          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path fileName = ev.context();

          // Only process CSV files
          if (fileName.toString().toLowerCase().endsWith(".csv")) {
            Path fullPath = Paths.get(importDirectory).resolve(fileName);

            if (kind == StandardWatchEventKinds.ENTRY_CREATE
                || kind == StandardWatchEventKinds.ENTRY_MODIFY) {

              logger.info("Detected new/modified CSV file: {}", fileName);
              processNewCsvFile(fullPath);
            }
          }
        }

        // Reset the key
        boolean valid = key.reset();
        if (!valid) {
          logger.warn("Watch key no longer valid, stopping monitoring");
          break;
        }

      } catch (InterruptedException e) {
        logger.info("Hotfolder monitoring interrupted");
        Thread.currentThread().interrupt();
        break;
      } catch (java.nio.file.ClosedWatchServiceException e) {
        logger.info("Hotfolder monitoring stopped due to WatchService closure.");
        break;
      } catch (Exception e) {
        logger.error("Error in hotfolder monitoring", e);
        // Continue monitoring even if individual file processing fails
      }
    }

    logger.info("Hotfolder monitoring stopped");
  }

  /** Process a newly detected CSV file */
  private void processNewCsvFile(Path csvFile) {
    try {
      // Wait a bit to ensure file is fully written
      Thread.sleep(1000);

      // Check if file exists and is readable
      if (!Files.exists(csvFile) || !Files.isReadable(csvFile)) {
        logger.warn("CSV file not accessible: {}", csvFile);
        return;
      }

      logger.info("Starting import of CSV file: {}", csvFile);

      // Import the CSV file
      int imported = csvImportService.importCsvFile(csvFile);

      logger.info("Successfully imported {} records from {}", imported, csvFile);

      // Check for failed favorite jobs and send notifications
      checkForFailedFavoriteJobs();

    } catch (Exception e) {
      logger.error("Failed to process CSV file: {}", csvFile, e);
    }
  }

  /** Check for recently failed favorite jobs and send notifications */
  private void checkForFailedFavoriteJobs() {
    try {
      // Get all favorites
      List<JobFavorite> favorites = favoriteRepository.findAll();

      for (JobFavorite favorite : favorites) {
        // Only check if failure notifications are enabled
        if (favorite.getNotifyOnFailure() == null || !favorite.getNotifyOnFailure()) {
          continue;
        }

        // Check if this job has recent failed executions
        // This will be implemented in the notification service
        notificationService.checkAndNotifyJobFailure(favorite);
      }

    } catch (Exception e) {
      logger.error("Error checking for failed favorite jobs", e);
    }
  }

  /** Event listener for job execution status changes */
  @EventListener
  public void handleJobExecutionEvent(JobExecutionEvent event) {
    ImportedJobExecution execution = event.getExecution();

    // Check if this is a FAIL status and job is favorited
    if ("FAIL".equals(execution.getStatus())) {
      List<JobFavorite> favorites = favoriteRepository.findByJobName(execution.getJobName());

      for (JobFavorite favorite : favorites) {
        if (favorite.getNotifyOnFailure() != null && favorite.getNotifyOnFailure()) {
          notificationService.sendJobFailureNotification(favorite, execution);
        }
      }
    }
  }

  /** Manual trigger for processing all CSV files in the import directory */
  public int processAllCsvFiles() throws IOException {
    Path importPath = Paths.get(importDirectory);
    int totalImported = 0;

    if (!Files.exists(importPath)) {
      logger.warn("Import directory does not exist: {}", importPath);
      return 0;
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(importPath, "*.csv")) {
      for (Path csvFile : stream) {
        try {
          logger.info("Processing CSV file: {}", csvFile);
          int imported = csvImportService.importCsvFile(csvFile);
          totalImported += imported;
          logger.info("Imported {} records from {}", imported, csvFile);
        } catch (Exception e) {
          logger.error("Failed to process CSV file: {}", csvFile, e);
        }
      }
    }

    // Check for failed favorite jobs after processing all files
    checkForFailedFavoriteJobs();

    return totalImported;
  }

  /** Get hotfolder monitoring status */
  public boolean isMonitoringActive() {
    return running.get();
  }

  /** Get import directory path */
  public String getImportDirectory() {
    return importDirectory;
  }
}
