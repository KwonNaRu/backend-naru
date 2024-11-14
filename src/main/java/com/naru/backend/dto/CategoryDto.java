package com.naru.backend.dto;

import java.util.List;

import com.naru.backend.model.Category;
import com.naru.backend.model.Post;

import lombok.Data;

@Data
public class CategoryDto {

    private Long id;
    private String name;
    private UserDto user; // User의 ID만 반환
    private List<Post> posts;

    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.user = new UserDto(
                category.getUser().getUserId(),
                category.getUser().getUsername(),
                category.getUser().getEmail());
        this.posts = category.getPosts();
    }
}
