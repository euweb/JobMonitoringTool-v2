package com.company.jobmonitor.repository;

import com.company.jobmonitor.entity.JobFavorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobFavoriteRepository extends JpaRepository<JobFavorite, Long> {

  /** Find all favorites for a user */
  List<JobFavorite> findByUserIdOrderByJobName(Integer userId);

  /** Find favorite by user and job name */
  Optional<JobFavorite> findByUserIdAndJobName(Integer userId, String jobName);

  /** Check if job is favorited by user */
  boolean existsByUserIdAndJobName(Integer userId, String jobName);

  /** Find all users who have favorited a specific job */
  @Query("SELECT f FROM JobFavorite f WHERE f.jobName = :jobName")
  List<JobFavorite> findByJobName(@Param("jobName") String jobName);

  /** Find favorites that should be notified on failure */
  @Query("SELECT f FROM JobFavorite f WHERE f.jobName = :jobName AND f.notifyOnFailure = true")
  List<JobFavorite> findFailureNotificationSubscribers(@Param("jobName") String jobName);

  /** Find favorites that should be notified on success */
  @Query("SELECT f FROM JobFavorite f WHERE f.jobName = :jobName AND f.notifyOnSuccess = true")
  List<JobFavorite> findSuccessNotificationSubscribers(@Param("jobName") String jobName);

  /** Find favorites that should be notified on start */
  @Query("SELECT f FROM JobFavorite f WHERE f.jobName = :jobName AND f.notifyOnStart = true")
  List<JobFavorite> findStartNotificationSubscribers(@Param("jobName") String jobName);

  /** Count total favorites for a user */
  long countByUserId(Integer userId);
}
