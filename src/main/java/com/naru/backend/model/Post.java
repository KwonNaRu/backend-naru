package com.naru.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "posts")
@Data
@NoArgsConstructor
public class Post {
    @Id
    private String id;
    private Long authorId;
    private String author;
    private String title;
    private String content;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Comment> comments; // 댓글을 포함

    @Data
    @NoArgsConstructor
    public static class Comment {
        private String id;
        private Long authorId;
        private String author;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Comment> replies; // 대댓글
    }
}