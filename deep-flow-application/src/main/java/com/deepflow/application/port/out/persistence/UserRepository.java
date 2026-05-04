package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAllById(List<Long> ids);

    Optional<User> findByUsername(String username);
}
