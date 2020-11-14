package org.suai.tracker_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.suai.tracker_test.model.Project;
import org.suai.tracker_test.model.User;
import org.suai.tracker_test.service.ProjectService;
import org.suai.tracker_test.service.UserService;

import java.util.List;

//FIXME resolve that problems:
// - Add cookies
// - Add authorization
//

@Controller
public class ProjectController {

    public final ProjectService projectService;
    public final UserService userService;

    @Autowired
    public ProjectController(ProjectService projectService, UserService userService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public String getAllProjects(@CurrentSecurityContext(expression = "authentication.name") String username, Model model) {
        User user = userService.findByLogin(username);
        List<Project> projects = user.getProjects(); // TODO find user by project that current user in
        model.addAttribute("projects", projects);
        return "projects/all";
    }

    @GetMapping("/create_project")
    public String getCreateForm(Project project) {
        return "projects/create";
    }

    @PostMapping("/create_project")
    public String createProject(Project project) {
        projectService.saveProject(project);
        return "redirect:/projects";
    }

    @GetMapping("/update_project/{id}")
    public String getUpdateForm(@PathVariable("id") Long id, Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("users", project.getUsers());
        return "projects/update";
    }

    @PostMapping("/update_project")
    public String updateProject(Project project) {
        projectService.saveProject(project);
        return "redirect:/projects";
    }

    @PostMapping("/update_project_add")
    public String updateProjectAddUser(@RequestParam("id") Long id,
                                       @RequestParam("user_login") String userLogin) {
        User user = userService.findByLogin(userLogin);
        Project project = projectService.findById(id);
        project.getUsers().add(user);
        user.getProjects().add(project);
        userService.save(user);
        projectService.saveProject(project);
        return "redirect:/projects";
    }

    @PostMapping("/update_project_remove")
    public String updateProjectRemoveUser(@RequestParam("id") Long id,
                                          @RequestParam("user_login") String userLogin) {
        User user = userService.findByLogin(userLogin);
        Project project = projectService.findById(id);
        project.getUsers().remove(user);
        user.getProjects().remove(project);
        userService.save(user);
        projectService.saveProject(project);
        return "redirect:/projects";
    }

}
