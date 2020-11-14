package org.suai.tracker_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.suai.tracker_test.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
