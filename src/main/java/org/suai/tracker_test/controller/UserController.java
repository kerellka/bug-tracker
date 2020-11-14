package org.suai.tracker_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.suai.tracker_test.model.User;
import org.suai.tracker_test.service.UserService;

@Controller
@RequestMapping("/me")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String getProfile(@CurrentSecurityContext(expression = "authentication.name") String username, Model model) {
        User user = userService.findByLogin(username);
        model.addAttribute("user", user);
        model.addAttribute("projects", user.getProjects());
        return "users/profile";
    }

    @PostMapping("/delete/{id}")
    public String deleteProfile(@PathVariable Long id) {
        userService.deleteById(id);
        return "auth/login";
    }
}
