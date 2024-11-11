package com.naru.backend.service;

import com.naru.backend.model.Comment;
import com.naru.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    // 특정 게시물의 모든 댓글 조회
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPost_PostId(postId);
    }

    // 댓글 생성
    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    // 댓글 수정
    public Comment updateComment(Long id, Comment comment) {
        comment.setCommentId(id);
        return commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}