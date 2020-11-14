package org.suai.tracker_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.suai.tracker_test.model.Status;
import org.suai.tracker_test.model.Ticket;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
