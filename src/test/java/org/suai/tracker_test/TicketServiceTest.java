package org.suai.tracker_test;

import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.suai.tracker_test.model.Project;
import org.suai.tracker_test.model.Status;
import org.suai.tracker_test.model.Ticket;
import org.suai.tracker_test.service.ProjectService;
import org.suai.tracker_test.service.TicketService;

@SpringBootTest
public class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Test
    @Transactional
    @Rollback
    void findAllTickets() {
        assertEquals(9, ticketService.findAll().size());
    }

    @Test
    @Transactional
    @Rollback
    void findTicketById() {
        assertNotNull(ticketService.findById((long) 1));
    }

}
