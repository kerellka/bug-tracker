package org.suai.tracker_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/open_list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getOpenTickets(Model model, @PathVariable String projectId) {
        List<Ticket> tickets = ticketService.findByStatus(Status.OPEN, projectService.findById(Long.parseLong(projectId)));
        model.addAttribute("tickets", tickets);
        return "tickets/open_list";
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
    }

    @GetMapping("/progress/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String moveToProgress(@PathVariable("id") Long id) {
        ticketService.setStatus(id, Status.IN_PROGRESS);
        return "redirect:/project/{projectId}/tickets/open_list";
    }

    @GetMapping("/close/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String moveToClose(@PathVariable("id") Long id) {
        ticketService.setStatus(id, Status.CLOSE);
        return "redirect:/project/{projectId}/tickets/progress_list";
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
        ticket.setAssignee(userService.findByLogin(username));
        ticket.setProject(projectService.findById(Long.parseLong(projectId)));
        ticketService.saveTicket(ticket);
        return "redirect:/project/{projectId}/tickets/open_list";
    }

    @GetMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUpdateForm(@PathVariable("id") Long id, Model model) {
        Ticket ticket = ticketService.findById(id);
        model.addAttribute("ticket", ticket);
        return "tickets/update";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTicket(@RequestParam("id") Long id, @RequestParam("assignee") String assignee,
                               @RequestParam("description") String description, @RequestParam("priority") Priority priority) {
        Ticket ticket = ticketService.findById(id);
        ticket.setPriority(priority);
        ticket.setAssignee(userService.findByLogin(assignee));
        ticket.setProject(projectService.findById((long)1));
        ticket.setDescription(description);
        ticketService.saveTicket(ticket);

        return correctToListRedirect(ticket);
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String deleteOpenTicket(@PathVariable("id") Long id) {
        String returnStr = correctToListRedirect(ticketService.findById(id));
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

    private String correctToListRedirect(Ticket ticket) {
        String returnStr;
        switch (ticket.getStatus()) {
            case OPEN:
                returnStr = "redirect:/tickets/open_list";
                break;
            case IN_PROGRESS:
                returnStr = "redirect:/tickets/progress_list";
                break;
            case CLOSE:
                returnStr = "redirect:/tickets/close_list";
                break;
            default:
                returnStr = "";
        }
        return returnStr;
    }

}
