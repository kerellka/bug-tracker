package org.suai.tracker_test.model;

import lombok.Data;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title")
    private String title;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private List<User> users = new LinkedList<>();
}
