package org.suai.tracker_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.exceptions.UserNotFoundException;
import org.suai.tracker_test.model.Project;
import org.suai.tracker_test.model.Role;
import org.suai.tracker_test.model.User;
import org.suai.tracker_test.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, @Qualifier("bcrypt") BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.getOne(id);
    }

    public User save(User user) {
        if (!existsWithLogin(user.getLogin())) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setRole(Role.USER);
        }
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User findByLogin(String login) throws RuntimeException {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getLogin().equals(login))
                .findAny().orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getEmail().equals(email))
                .findAny().orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean existsWithEmail(String email) {
        boolean result = false;

        try {
            findByEmail(email);
            result = true;
        } catch (UserNotFoundException ignored) {
        }

        return result;
    }

    public boolean existsWithLogin(String login) {
        boolean result = false;

        try {
            findByLogin(login);
            result = true;
        } catch (UserNotFoundException ignored) {
        }

        return result;
    }

   /* public List<User> findAllByProject(Project project) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getProject() != null && user.getProject().equals(project))
                .collect(Collectors.toList());
    }*/

}
