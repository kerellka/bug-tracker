package org.suai.tracker_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.suai.tracker_test.model.Timeline;

public interface TimelineRepository extends JpaRepository<Timeline, Long> {
}
