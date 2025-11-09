package com.company.jobmonitor.service;

import com.company.jobmonitor.entity.ImportedJobExecution;
import com.company.jobmonitor.repository.ImportedJobExecutionRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for importing job execution data from CSV files */
@Service
public class CsvImportService {

  private static final Logger logger = LoggerFactory.getLogger(CsvImportService.class);

  @Autowired private ImportedJobExecutionRepository executionRepository;

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Value("${app.csv.import.directory:./import}")
  private String importDirectory;

  @Value("${app.csv.import.processed-directory:./import/processed}")
  private String processedDirectory;

  // CSV column indices based on your provided structure
  private static final int COL_ID = 0;
  private static final int COL_TYP = 1;
  private static final int COL_NAME = 2;
  private static final int COL_SCRIPT = 3;
  private static final int COL_PRIO = 4;
  private static final int COL_STRAT = 5;
  private static final int COL_STATUS = 6;
  private static final int COL_VON = 7;
  private static final int COL_AM = 8;
  private static final int COL_GESTARTET = 9;
  private static final int COL_BEENDET = 10;
  private static final int COL_AUF = 11;
  private static final int COL_PARENT = 12;
  private static final int COL_LAUFZEIT = 13;

  private static final DateTimeFormatter[] DATE_FORMATTERS = {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  };

  /** Import all CSV files from the import directory */
  @Transactional
  public int importAllCsvFiles() {
    try {
      Path importPath = Paths.get(importDirectory);
      if (!Files.exists(importPath)) {
        Files.createDirectories(importPath);
        logger.info("Created import directory: {}", importDirectory);
      }

      Path processedPath = Paths.get(processedDirectory);
      if (!Files.exists(processedPath)) {
        Files.createDirectories(processedPath);
        logger.info("Created processed directory: {}", processedDirectory);
      }

      int totalImported = 0;
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(importPath, "*.csv")) {
        for (Path file : stream) {
          int imported = importCsvFile(file);
          totalImported += imported;

          // Move processed file
          Path targetPath = processedPath.resolve(file.getFileName());
          Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
          logger.info("Moved {} to processed directory", file.getFileName());
        }
      }

      logger.info("Total executions imported: {}", totalImported);
      return totalImported;

    } catch (IOException e) {
      logger.error("Error importing CSV files", e);
      throw new RuntimeException("Failed to import CSV files", e);
    }
  }

  /** Import a single CSV file */
  @Transactional
  public int importCsvFile(Path csvFile) throws IOException {
    logger.info("Starting import of CSV file: {}", csvFile.getFileName());

    List<ImportedJobExecution> executionsToSave = new ArrayList<>();
    List<Long> newExecutionIds = new ArrayList<>();
    int lineNumber = 0;
    int newCount = 0;
    int updatedCount = 0;

    try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
      String line;
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        // Skip header line
        if (lineNumber == 1) {
          continue;
        }

        try {
          ImportedJobExecution execution = parseCsvLine(line, csvFile.getFileName().toString());
          if (execution != null) {
            // Check if execution already exists
            ImportedJobExecution existingExecution =
                executionRepository.findByExecutionId(execution.getExecutionId());
            if (existingExecution != null) {
              // Update existing execution with latest data
              updateExistingExecution(existingExecution, execution);
              executionsToSave.add(existingExecution);
              updatedCount++;
              logger.debug("Updated execution {}", execution.getExecutionId());
            } else {
              // New execution
              executionsToSave.add(execution);
              newExecutionIds.add(execution.getExecutionId());
              newCount++;
              logger.debug("New execution {}", execution.getExecutionId());
            }
          }
        } catch (Exception e) {
          logger.warn(
              "Failed to parse line {} in file {}: {}",
              lineNumber,
              csvFile.getFileName(),
              e.getMessage());
        }
      }
    }

    // Batch save executions (new and updated)
    if (!executionsToSave.isEmpty()) {
      List<ImportedJobExecution> savedExecutions = executionRepository.saveAll(executionsToSave);

      // Fire events for new and updated executions
      for (ImportedJobExecution execution : savedExecutions) {
        if (newExecutionIds.contains(execution.getExecutionId())) {
          eventPublisher.publishEvent(
              new JobExecutionEvent(this, execution, JobExecutionEvent.EventType.CREATED));
        } else {
          eventPublisher.publishEvent(
              new JobExecutionEvent(this, execution, JobExecutionEvent.EventType.UPDATED));
        }
      }
    }

    logger.info(
        "Processed {} executions from {} ({} new, {} updated)",
        newCount + updatedCount,
        csvFile.getFileName(),
        newCount,
        updatedCount);

    return newCount + updatedCount;
  }

  /** Update existing execution with latest data from CSV */
  private void updateExistingExecution(ImportedJobExecution existing, ImportedJobExecution latest) {
    // Update fields that can change over time
    existing.setStatus(latest.getStatus());
    existing.setStartedAt(latest.getStartedAt());
    existing.setEndedAt(latest.getEndedAt());
    existing.setDurationSeconds(latest.getDurationSeconds());

    // Update import metadata
    existing.setCsvSourceFile(latest.getCsvSourceFile());
    existing.setImportTimestamp(LocalDateTime.now());

    // Update other fields that might change
    existing.setPriority(latest.getPriority());
    existing.setStrategy(latest.getStrategy());
    existing.setHost(latest.getHost());

    logger.debug(
        "Updated execution {} from status '{}' to '{}'",
        existing.getExecutionId(),
        existing.getStatus(),
        latest.getStatus());
  }

  /** Parse a CSV line properly handling quoted values (Excel format) */
  private String[] parseCsvLineWithQuotes(String line) {
    List<String> columns = new ArrayList<>();
    StringBuilder currentColumn = new StringBuilder();
    boolean insideQuotes = false;
    int i = 0;

    while (i < line.length()) {
      char ch = line.charAt(i);

      if (ch == '"') {
        if (insideQuotes) {
          // Check for escaped quote (double quotes)
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            currentColumn.append('"');
            i += 2; // Skip both quotes
            continue;
          } else {
            // End of quoted field
            insideQuotes = false;
          }
        } else {
          // Start of quoted field
          insideQuotes = true;
        }
      } else if (ch == ',' && !insideQuotes) {
        // End of column
        columns.add(currentColumn.toString().trim());
        currentColumn.setLength(0);
      } else {
        // Regular character
        currentColumn.append(ch);
      }

      i++;
    }

    // Add the last column
    columns.add(currentColumn.toString().trim());

    // Convert to array
    String[] result = columns.toArray(new String[0]);

    // Log parsing result for debugging
    logger.debug("Parsed CSV line: {} columns from line: {}", result.length, line);

    return result;
  }

  /** Parse a single CSV line into an ImportedJobExecution */
  private ImportedJobExecution parseCsvLine(String line, String sourceFile) {
    // Handle different line endings (Windows CRLF vs Unix LF)
    line = line.replaceAll("\\r\\n|\\r|\\n", "").trim();

    // Parse CSV line properly handling quoted values (Windows Excel format)
    String[] columns = parseCsvLineWithQuotes(line);

    if (columns.length < 14) {
      logger.warn(
          "Invalid CSV line format: insufficient columns ({}), expected 14", columns.length);
      return null;
    }

    try {
      ImportedJobExecution execution = new ImportedJobExecution();

      // Parse ID with better error handling
      String idString = columns[COL_ID];
      execution.setExecutionId(parseStringToLong(idString));

      // Skip if ID is null or 0
      if (execution.getExecutionId() == null || execution.getExecutionId() == 0) {
        logger.warn("Invalid execution ID in CSV line: '{}', full line: {}", idString, line);
        return null;
      }

      // Parse basic fields
      execution.setJobType(parseString(columns[COL_TYP]));
      execution.setJobName(parseString(columns[COL_NAME]));
      execution.setScriptPath(parseString(columns[COL_SCRIPT]));
      execution.setPriority(parseStringToInteger(columns[COL_PRIO]));
      execution.setStrategy(parseStringToInteger(columns[COL_STRAT]));
      execution.setStatus(parseString(columns[COL_STATUS]));
      execution.setSubmittedBy(parseString(columns[COL_VON]));
      execution.setHost(parseString(columns[COL_AUF]));

      // Parse parent execution ID
      execution.setParentExecutionId(parseStringToLong(columns[COL_PARENT]));

      // Parse duration
      execution.setDurationSeconds(parseStringToLong(columns[COL_LAUFZEIT]));

      // Parse timestamps
      execution.setSubmittedAt(parseTimestamp(columns[COL_AM]));
      execution.setStartedAt(parseTimestamp(columns[COL_GESTARTET]));
      execution.setEndedAt(parseTimestamp(columns[COL_BEENDET]));

      // Set import metadata
      execution.setCsvSourceFile(sourceFile);

      return execution;

    } catch (Exception e) {
      logger.warn("Error parsing CSV line: {}", e.getMessage());
      return null;
    }
  }

  /** Parse string value, handling empty/null */
  private String parseString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }

  /** Parse string to Long, handling empty/null */
  private Long parseStringToLong(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Parse string to Integer, handling empty/null */
  private Integer parseStringToInteger(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Parse timestamp with multiple format support */
  private LocalDateTime parseTimestamp(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    String trimmed = value.trim();
    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        return LocalDateTime.parse(trimmed, formatter);
      } catch (DateTimeParseException ignored) {
        // Try next formatter
      }
    }

    logger.warn("Failed to parse timestamp: {}", value);
    return null;
  }

  /** Get import directory for manual file placement */
  public String getImportDirectory() {
    return importDirectory;
  }

  /** Get processed directory */
  public String getProcessedDirectory() {
    return processedDirectory;
  }
}
