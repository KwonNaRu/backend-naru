package com.naru.backend.dto;

import com.naru.backend.model.Post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostDTO {
    private Long postId;
    private String author; // 작성자 ID (User 엔티티와의 관계)
    private String title; // 제목
    private String content; // 내용
    private Long categoryId; // 카테고리

    public PostDTO(Post post) {
        this.postId = post.getPostId();
        this.author = post.getUser().getUsername();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.categoryId = (post.getCategory() != null) ? post.getCategory().getId() : null;
    }
}
