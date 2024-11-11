package com.naru.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId; // 댓글 ID

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 댓글이 달린 게시물

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 댓글 작성자

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonBackReference // 부모 댓글을 참조하는 필드
    private Comment parent; // 부모 댓글 (대댓글의 경우)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // 자식 댓글 리스트
    private List<Comment> replies = new ArrayList<>(); // 자식 댓글 리스트

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

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