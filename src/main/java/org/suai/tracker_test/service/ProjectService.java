package org.suai.tracker_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.model.Project;
import org.suai.tracker_test.model.User;
import org.suai.tracker_test.repository.ProjectRepository;

import java.util.LinkedList;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project findById(Long id) {
        return projectRepository.getOne(id);
    }

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }

    public List<List<User>> getUsersInProjects() {
        List<Project> projects = projectRepository.findAll();
        List<List<User>> userLists = new LinkedList<>();
        projects.forEach(project -> userLists.add(project.getUsers()));
        return userLists;
    }
}
