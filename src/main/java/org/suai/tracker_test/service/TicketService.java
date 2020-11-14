package org.suai.tracker_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.model.Project;
import org.suai.tracker_test.model.Status;
import org.suai.tracker_test.model.Ticket;
import org.suai.tracker_test.repository.TicketRepository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Ticket findById(Long id) {
        return ticketRepository.getOne(id);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    public List<Ticket> findByStatus(Status status, Project project) {
        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getStatus().equals(status) && ticket.getProject().equals(project))
                .collect(Collectors.toList());
    }

    public void setStatus(Long id, Status newStatus) {
        Ticket ticket = ticketRepository.getOne(id);
        ticket.setStatus(newStatus);
        if (newStatus.equals(Status.CLOSE)) {
            ticket.setCloseDate(new Date(System.currentTimeMillis()));
        }
        ticketRepository.save(ticket);
    }
}
