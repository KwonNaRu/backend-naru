package com.naru.backend.graphql;

import java.util.List;

import org.springframework.stereotype.Component;

import com.naru.backend.model.Category;
import com.naru.backend.model.Post;
import com.naru.backend.repository.CategoryRepository;
import com.naru.backend.repository.PostRepository;

import graphql.kickstart.tools.GraphQLQueryResolver;

@Component
public class QueryResolver implements GraphQLQueryResolver {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public QueryResolver(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Post> posts() {
        return postRepository.findAll();
    }

    public List<Category> categories() {
        return categoryRepository.findAll();
    }
}