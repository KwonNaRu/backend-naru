package com.naru.backend.controller;

import com.naru.backend.model.Comment;
import com.naru.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 특정 게시물의 모든 댓글 조회
    @GetMapping("/post/{postId}")
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    // 댓글 생성
    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.createComment(comment);
    }

    // 댓글 수정
    @PutMapping("/{id}")
    public Comment updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        return commentService.updateComment(id, comment);
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }
}