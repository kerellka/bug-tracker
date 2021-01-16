package org.suai.tracker_test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.suai.tracker_test.model.User;
import org.suai.tracker_test.service.UserService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String checkCredits(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        if (userService.checkCredits(username, password)) {
            model.addAttribute("loginError", "Incorrect data!");
            return "redirect:/auth/login";
        }
        return "auth/login";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("user") User user, Model model) {

        if (!user.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            model.addAttribute("passwordIncorrectSymbols", "Password must contain 8 characters, at least one letter and one number");
            return "auth/registration";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())){
            model.addAttribute("passwordError", "Passwords doesn't match");
            return "auth/registration";
        }

        if(userService.existsWithEmail(user.getEmail())){
            model.addAttribute("emailError", "User with that email already exists");
            return "auth/registration";
        }

        if (userService.existsWithLogin(user.getLogin())){
            model.addAttribute("loginError", "User with that login already exists");
            return "auth/registration";
        }

        userService.save(user);

        return "redirect:/projects";
    }
}
