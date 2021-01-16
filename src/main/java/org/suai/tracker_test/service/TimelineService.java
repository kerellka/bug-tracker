package org.suai.tracker_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.model.Project;
import org.suai.tracker_test.model.Ticket;
import org.suai.tracker_test.model.Timeline;
import org.suai.tracker_test.repository.TimelineRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineService {

    private final TimelineRepository timelineRepository;

    @Autowired
    public TimelineService(TimelineRepository timelineRepository) {
        this.timelineRepository = timelineRepository;
    }

    public List<Timeline> findAll() {
        return timelineRepository.findAll();
    }

    public Timeline findById(Long id) {
        return timelineRepository.getOne(id);
    }

    public Timeline saveTicket(Timeline timeline) {
        return timelineRepository.save(timeline);
    }

    public void deleteById(Long id) {
        timelineRepository.deleteById(id);
    }

    public Timeline saveTimeline(Timeline timeline) {
        return timelineRepository.save(timeline);
    }

    public List<Timeline> findAllInProject(Project project) {
        return timelineRepository.findAll()
                .stream()
                .filter(timeline -> timeline.getProject().equals(project))
                .sorted(Collections.reverseOrder(Comparator.comparing(Timeline::getActionDate)
                        .thenComparing(Timeline::getActionTime)))
                .collect(Collectors.toList());
    }
}
