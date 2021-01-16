package org.suai.tracker_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.suai.tracker_test.model.Comment;
import org.suai.tracker_test.model.Ticket;
import org.suai.tracker_test.repository.CommentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Comment findById(Long id) {
        return commentRepository.getOne(id);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    public List<Comment> findAllForTicket(Ticket ticket) {
        return commentRepository.findAll()
                .stream()
                .filter(comment -> comment.getTicket().equals(ticket))
                .collect(Collectors.toList());
    }

}
