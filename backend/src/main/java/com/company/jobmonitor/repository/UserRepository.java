package com.company.jobmonitor.repository;

import com.company.jobmonitor.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsernameAndIdNot(String username, Integer id);

  boolean existsByEmailAndIdNot(String email, Integer id);

  @Query(
      "SELECT u FROM User u WHERE "
          + "(:search IS NULL OR :search = '' OR "
          + "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR "
          + "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
          + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
          + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<User> findUsersWithSearch(@Param("search") String search, Pageable pageable);

  @Query("SELECT u FROM User u WHERE u.role = :role")
  Page<User> findByRole(@Param("role") User.Role role, Pageable pageable);

  @Query("SELECT u FROM User u WHERE u.enabled = :enabled")
  Page<User> findByEnabled(@Param("enabled") boolean enabled, Pageable pageable);

  @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
  long countByRole(@Param("role") User.Role role);
}
