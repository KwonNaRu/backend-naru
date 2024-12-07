package com.naru.backend.dto;

import java.util.List;

import com.naru.backend.model.Category;
import com.naru.backend.model.Post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private String userEmail;
    private String userName;
    private List<Post> posts;

    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.userEmail = category.getUser().getEmail();
        this.userName = category.getUser().getUsername();
        this.posts = category.getPosts();
    }
}
