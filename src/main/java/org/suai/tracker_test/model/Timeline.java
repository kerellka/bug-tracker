package org.suai.tracker_test.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;

@Data
@Entity
@Table(name = "timeline")
public class Timeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "action")
    private Action action;

    @Column(name = "action_date")
    private Date actionDate;

    @Column(name = "action_time")
    private Time actionTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    @PrePersist
    public void onCreate() {
        actionDate = new Date(System.currentTimeMillis());
        actionTime = new Time(System.currentTimeMillis());
    }
}
