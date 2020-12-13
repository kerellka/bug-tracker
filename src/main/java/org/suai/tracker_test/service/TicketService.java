package org.suai.tracker_test.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.model.*;
import org.suai.tracker_test.repository.TicketRepository;

import java.sql.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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

    public List<Ticket> findAllInProject(Project project) {
        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getProject().equals(project) && !ticket.getStatus().equals(Status.DELETED))
                .collect(Collectors.toList());
    }

    public List<Ticket> filterByTypeAndPriority(List<Ticket> input, Set<Type> types, Set<Priority> priorities) {
        return input
                .stream()
                .filter(ticket -> types.isEmpty() || (types.contains(ticket.getType())))
                .filter(ticket -> priorities.isEmpty() || priorities.contains(ticket.getPriority()))
                .collect(Collectors.toList());
    }

    public List<Ticket> getSortedBy(Status status, Project project, String sort) {
        Stream<Ticket> ticketStream = ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getStatus().equals(status) && ticket.getProject().equals(project));

        switch(sort){
            case "dateASC":
                return ticketStream
                        .sorted((Comparator.comparing(Ticket::getOpenDate)))
                        .collect(Collectors.toList());
            case "dateDESC":
                return ticketStream
                        .sorted((Collections.reverseOrder(Comparator.comparing(Ticket::getOpenDate))))
                        .collect(Collectors.toList());
            case "priorityASC":
                return ticketStream
                        .sorted((Comparator.comparing(Ticket::getPriority)))
                        .collect(Collectors.toList());
            case "priorityDESC":
                return ticketStream
                        .sorted((Collections.reverseOrder(Comparator.comparing(Ticket::getPriority))))
                        .collect(Collectors.toList());
            default:
                return ticketStream.collect(Collectors.toList());

        }
    }

    public int countProgress(Project project) {
        List<Ticket> all = findAllInProject(project);
        List<Ticket> closed = findByStatus(Status.CLOSE, project);

        return (closed.size() * 100) / all.size();
    }

    public long countWithStatus(Project project, Status status) {
        return findAll().stream()
                .filter(ticket -> ticket.getStatus().equals(status) && ticket.getProject().equals(project))
                .count();
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
