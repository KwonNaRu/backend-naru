package com.naru.backend.repository;

import com.naru.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostId(Long postId); // 특정 게시물의 댓글 조회
}