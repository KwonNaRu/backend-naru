package com.naru.backend.dto;

import java.util.List;

import com.naru.backend.model.Category;
import com.naru.backend.model.Post;

import lombok.Data;

@Data
public class CategoryDto {

    private Long id;
    private String name;
    private UserResponseDTO user;
    private List<Post> posts;

    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.user = new UserResponseDTO(category.getUser());
        this.posts = category.getPosts();
    }
}
