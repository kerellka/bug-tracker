package org.suai.tracker_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.suai.tracker_test.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
