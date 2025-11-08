package com.company.jobmonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Scheduler service for automatic CSV import */
@Service
public class CsvImportScheduler {

  private static final Logger logger = LoggerFactory.getLogger(CsvImportScheduler.class);

  @Autowired private CsvImportService csvImportService;

  /** Automatically import CSV files every 5 minutes */
  @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
  public void scheduleAutoImport() {
    try {
      logger.debug("Starting scheduled CSV import");
      int imported = csvImportService.importAllCsvFiles();

      if (imported > 0) {
        logger.info("Scheduled import completed: {} new executions imported", imported);
      }

    } catch (Exception e) {
      logger.error("Error during scheduled CSV import", e);
    }
  }

  /** Import CSVs every hour for regular monitoring */
  @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
  public void scheduleHourlyImport() {
    try {
      logger.info("Starting hourly CSV import check");
      csvImportService.importAllCsvFiles();
    } catch (Exception e) {
      logger.error("Error during hourly CSV import", e);
    }
  }
}
