package org.suai.tracker_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.suai.tracker_test.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByLogin(String login);
}
