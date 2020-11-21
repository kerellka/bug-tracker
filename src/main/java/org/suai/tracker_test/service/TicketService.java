package org.suai.tracker_test.service;

import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.model.*;
import org.suai.tracker_test.repository.TicketRepository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<Ticket> findByStatusAndType(Status status, Project project, Type type) {
        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getStatus().equals(status) &&
                        ticket.getProject().equals(project) &&
                        ticket.getType().equals(type))
                .collect(Collectors.toList());
    }

    public List<Ticket> findByStatusAndPriority(Status status, Project project, Priority priority) {
        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getStatus().equals(status) &&
                        ticket.getProject().equals(project) &&
                        ticket.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    public List<Ticket> getSortedByOpenDate(Status status, Project project, String increase) {
        return ticketRepository.findAll(Sort.by(increase.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "openDate"))
                .stream()
                .filter(ticket -> ticket.getStatus().equals(status) && ticket.getProject().equals(project))
                .collect(Collectors.toList());
    }

    public List<Ticket> getSortedByPriority(Status status, Project project, String increase) {
        Stream<Ticket> ticketStream = ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getStatus().equals(status) && ticket.getProject().equals(project));

        if (increase.equals("asc")) {
            return ticketStream
                    .sorted((Comparator.comparing(Ticket::getPriority)))
                    .collect(Collectors.toList());
        } else {
            return ticketStream
                    .sorted((Collections.reverseOrder(Comparator.comparing(Ticket::getPriority))))
                    .collect(Collectors.toList());
        }
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
