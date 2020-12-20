package org.suai.tracker_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.suai.tracker_test.exceptions.UserNotFoundException;
import org.suai.tracker_test.model.*;
import org.suai.tracker_test.repository.CommentRepository;
import org.suai.tracker_test.service.*;

import java.util.*;

@Controller
@RequestMapping("/project/{projectId}/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final ProjectService projectService;
    private final TimelineService timelineService;
    private final CommentService commentService;

    @Autowired
    public TicketController(TicketService ticketService, UserService userService,
                            ProjectService projectService, TimelineService timelineService,
                            CommentService commentService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.projectService = projectService;
        this.timelineService = timelineService;
        this.commentService = commentService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getTickets(Model model, @PathVariable String projectId, @RequestParam("status") String status,
                             @RequestParam(value = "sort", required = false, defaultValue = "default") String sort,
                             @RequestParam(value = "type", required = false) String[] typesString,
                             @RequestParam(value = "priority", required = false) String[] prioritiesString) {

        Set<Type> types = new HashSet<>();
        Set<Priority> priorities = new HashSet<>();

        if (typesString != null) {
            Arrays.stream(typesString).forEach(typeName -> types.add(Type.valueOf(typeName)));
        }
        if (prioritiesString != null) {
            Arrays.stream(prioritiesString).forEach(priorityName -> priorities.add(Priority.valueOf(priorityName)));
        }

        List<Ticket> tickets = ticketService.getSortedBy(Status.valueOf(status),
                projectService.findById(Long.parseLong(projectId)), sort);

        tickets = ticketService.filterByTypeAndPriority(tickets, types, priorities);

        model.addAttribute("tickets", tickets);
        model.addAttribute("status", Status.valueOf(status));
        return "tickets/all";
    }

    @GetMapping("/progress")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String getProgressBar(Model model, @PathVariable String projectId) {
        Project project = projectService.findById(Long.parseLong(projectId));
        model.addAttribute("progress", ticketService.countProgress(project));
        model.addAttribute("timeline", timelineService.findAllInProject(project));
        model.addAttribute("open", ticketService.countWithStatus(project, Status.OPEN));
        model.addAttribute("in_progress", ticketService.countWithStatus(project, Status.IN_PROGRESS));
        model.addAttribute("close", ticketService.countWithStatus(project, Status.CLOSE));
        return "tickets/progress";
    }

    @GetMapping("/progress/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String moveToProgress(@CurrentSecurityContext(expression = "authentication.name") String username,
                                 @PathVariable Long id,
                                 @PathVariable String projectId) {
        ticketService.setStatus(id, Status.IN_PROGRESS);

        Timeline timeline = new Timeline();
        timeline.setAction(Action.MOVE_IN_PROGRESS);
        timeline.setUser(userService.findByLogin(username));
        timeline.setProject(projectService.findById(Long.parseLong(projectId)));
        timeline.setTicket(ticketService.findById(id));
        timelineService.saveTimeline(timeline);

        return "redirect:/project/{projectId}/tickets/list?status=OPEN";
    }

    @GetMapping("/close/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String moveToClose(@CurrentSecurityContext(expression = "authentication.name") String username,
                              @PathVariable Long id,
                              @PathVariable String projectId) {
        ticketService.setStatus(id, Status.CLOSE);

        Timeline timeline = new Timeline();
        timeline.setAction(Action.CLOSE);
        timeline.setUser(userService.findByLogin(username));
        timeline.setProject(projectService.findById(Long.parseLong(projectId)));
        timeline.setTicket(ticketService.findById(id));
        timelineService.saveTimeline(timeline);

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

        Timeline timeline = new Timeline();
        timeline.setAction(Action.CREATE);
        timeline.setUser(userService.findByLogin(username));
        timeline.setProject(projectService.findById(Long.parseLong(projectId)));
        timeline.setTicket(ticket);
        timelineService.saveTimeline(timeline);

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
    public String updateTicket(@CurrentSecurityContext(expression = "authentication.name") String username,
                               @RequestParam Long id,
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

        Timeline timeline = new Timeline();
        timeline.setAction(Action.UPDATE);
        timeline.setUser(userService.findByLogin(username));
        timeline.setProject(projectService.findById(Long.parseLong(projectId)));
        timeline.setTicket(ticket);
        timelineService.saveTimeline(timeline);

        return correctToListRedirect(ticket, projectId);
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String deleteOpenTicket(@CurrentSecurityContext(expression = "authentication.name") String username,
                                   @PathVariable("id") Long id,
                                   @PathVariable String projectId) {

        Timeline timeline = new Timeline();
        timeline.setAction(Action.DELETE);
        timeline.setUser(userService.findByLogin(username));
        timeline.setProject(projectService.findById(Long.parseLong(projectId)));
        timeline.setTicket(ticketService.findById(id));
        timelineService.saveTimeline(timeline);

        String returnStr = correctToListRedirect(ticketService.findById(id), projectId);
        ticketService.setStatus(id, Status.DELETED);
        return returnStr;
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String showDetails(@PathVariable("id") Long id, Model model, Comment comment) {
        Ticket ticket = ticketService.findById(id);
        model.addAttribute("ticket", ticket);
        List<Comment> comments = commentService.findAllForTicket(ticket);
        Collections.reverse(comments);
        model.addAttribute("comments", comments);
        return "tickets/details";
    }

    @PostMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String postComment(@PathVariable("id") Long id,
                              Comment comment,
                              @PathVariable("projectId") String projectId,
                              @CurrentSecurityContext(expression = "authentication.name") String username) {
        comment.setTicket(ticketService.findById(id));
        comment.setUser(userService.findByLogin(username));
        commentService.saveComment(comment);
        return "redirect:/project/{projectId}/tickets/details/{id}";
    }


    @GetMapping("/take/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String setAssigneeCurrentUser(@CurrentSecurityContext(expression = "authentication.name") String username,
                                         @PathVariable Long id,
                                         @PathVariable String projectId) {
        Ticket ticket = ticketService.findById(id);
        ticket.setAssignee(userService.findByLogin(username));
        ticketService.saveTicket(ticket);

        Timeline timeline = new Timeline();
        timeline.setAction(Action.PICK_UP);
        timeline.setUser(userService.findByLogin(username));
        timeline.setProject(projectService.findById(Long.parseLong(projectId)));
        timeline.setTicket(ticket);
        timelineService.saveTimeline(timeline);

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
