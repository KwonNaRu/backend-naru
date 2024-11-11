package com.naru.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId; // 글 ID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자 ID (User 엔티티와의 관계)

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 내용

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 카테고리

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments; // 댓글 리스트

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}