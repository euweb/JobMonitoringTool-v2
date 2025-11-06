package com.company.jobmonitor.service;

import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import com.company.jobmonitor.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    System.out.println("Loading user by username: " + username);

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    return UserPrincipal.create(user);
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserById(Integer id) throws UsernameNotFoundException {
    System.out.println("Loading user by id: " + id);

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

    return UserPrincipal.create(user);
  }
}
