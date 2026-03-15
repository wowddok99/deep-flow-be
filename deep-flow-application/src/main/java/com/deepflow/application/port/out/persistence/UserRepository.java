package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.user.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);
}
