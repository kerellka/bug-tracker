package org.suai.tracker_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.suai.tracker_test.exceptions.UserNotFoundException;
import org.suai.tracker_test.model.*;
import org.suai.tracker_test.service.ProjectService;
import org.suai.tracker_test.service.TicketService;
import org.suai.tracker_test.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/project/{projectId}/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final ProjectService projectService;

    @Autowired
    public TicketController(TicketService ticketService, UserService userService, ProjectService projectService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getTickets(Model model, @PathVariable String projectId,
                             @RequestParam("status") String status) {
        List<Ticket> tickets = ticketService.findByStatus(Status.valueOf(status), projectService.findById(Long.parseLong(projectId)));
        model.addAttribute("tickets", tickets);
        model.addAttribute("status", Status.valueOf(status));
        return "tickets/all";
    }

   /* @GetMapping("/open_list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getOpenTickets(Model model, @PathVariable String projectId) {
        List<Ticket> tickets = ticketService.findByStatus(Status.OPEN, projectService.findById(Long.parseLong(projectId)));
        model.addAttribute("tickets", tickets);
        return "tickets/open_list";
    }

    @GetMapping("/close_list/sortedDate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getOpenTicketsByDate(Model model, @PathVariable String projectId) {
        List<Ticket> tickets = ticketService.getSortedByOpenDate(Status.CLOSE, projectService.findById(Long.parseLong(projectId)), "asc");
        model.addAttribute("tickets", tickets);
        return "tickets/close_list";
    }

    @GetMapping("/close_list/sortedPriority")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getOpenTicketsByPriority(Model model, @PathVariable String projectId) {
        List<Ticket> tickets = ticketService.getSortedByPriority(Status.CLOSE, projectService.findById(Long.parseLong(projectId)), "asc");
        model.addAttribute("tickets", tickets);
        return "tickets/close_list";
    }

    @GetMapping("/close_list/withPriority{priority}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getOpenTicketsByPriority(Model model, @PathVariable String projectId,
                                           @PathVariable String priority) {
        List<Ticket> tickets = ticketService.findByStatusAndPriority(Status.CLOSE, projectService.findById(Long.parseLong(projectId)), Priority.valueOf(priority));
        model.addAttribute("tickets", tickets);
        return "tickets/close_list";
    }

    @GetMapping("/progress_list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getProgressTickets(Model model, @PathVariable String projectId) {
        List<Ticket> tickets = ticketService.findByStatus(Status.IN_PROGRESS, projectService.findById(Long.parseLong(projectId)) );
        model.addAttribute("tickets", tickets);
        return "tickets/progress_list";
    }

    @GetMapping("/close_list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getCloseTickets(Model model, @PathVariable String projectId) {
        List<Ticket> tickets = ticketService.findByStatus(Status.CLOSE, projectService.findById(Long.parseLong(projectId)));
        model.addAttribute("tickets", tickets);
        return "tickets/close_list";
    }*/

    @GetMapping("/progress/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String moveToProgress(@PathVariable Long id) {
        ticketService.setStatus(id, Status.IN_PROGRESS);
        return "redirect:/project/{projectId}/tickets/list?status=OPEN";
    }

    @GetMapping("/close/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String moveToClose(@PathVariable Long id) {
        ticketService.setStatus(id, Status.CLOSE);
        return "redirect:/project/{projectId}/tickets/list?status=IN_PROGRESS";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getCreateForm(Ticket ticket, Type type, Priority priority) {
        return "tickets/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String createTicket(@CurrentSecurityContext(expression = "authentication.name") String username,
                               Ticket ticket, @PathVariable String projectId) {
        ticket.setReporter(userService.findByLogin(username));
        ticket.setAssignee(null);
        ticket.setProject(projectService.findById(Long.parseLong(projectId)));
        ticketService.saveTicket(ticket);
        return "redirect:/project/{projectId}/tickets/list?status=OPEN";
    }

    @GetMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUpdateForm(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.findById(id);
        model.addAttribute("ticket", ticket);
        return "tickets/update";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTicket(@RequestParam Long id,
                               @RequestParam String assignee,
                               @RequestParam String description,
                               @RequestParam Priority priority,
                               @PathVariable String projectId) {
        Ticket ticket = ticketService.findById(id);
        ticket.setPriority(priority);
        try {
            ticket.setAssignee(userService.findByLogin(assignee));
        } catch (UserNotFoundException e) {
            ticket.setAssignee(null);
        }
        ticket.setProject(projectService.findById(Long.parseLong(projectId)));
        ticket.setDescription(description);
        ticketService.saveTicket(ticket);

        return correctToListRedirect(ticket, projectId);
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String deleteOpenTicket(@PathVariable("id") Long id,
                                   @PathVariable String projectId) {
        String returnStr = correctToListRedirect(ticketService.findById(id), projectId);
        ticketService.deleteById(id);
        return returnStr;
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String showDetails(@PathVariable("id") Long id, Model model) {
        Ticket ticket = ticketService.findById(id);
        model.addAttribute("ticket", ticket);
        return "tickets/details";
    }

    @GetMapping("/take/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String setAssigneeCurrentUser(@CurrentSecurityContext(expression = "authentication.name") String username,
                                         @PathVariable Long id) {
        Ticket ticket = ticketService.findById(id);
        ticket.setAssignee(userService.findByLogin(username));
        ticketService.saveTicket(ticket);
        return "redirect:/project/{projectId}/tickets/list?status=OPEN";
    }

    private String correctToListRedirect(Ticket ticket, String projectId) {
        String returnStr;
        switch (ticket.getStatus()) {
            case OPEN:
                returnStr = "redirect:/project/" + projectId + "/tickets/list?status=OPEN";
                break;
            case IN_PROGRESS:
                returnStr = "redirect:/project/" + projectId + "/tickets/list?status=IN_PROGRESS";
                break;
            case CLOSE:
                returnStr = "redirect:/project/" + projectId + "/tickets/list?status=CLOSE";
                break;
            default:
                returnStr = "";
        }
        return returnStr;
    }

}
