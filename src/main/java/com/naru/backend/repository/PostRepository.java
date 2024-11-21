package com.naru.backend.repository;

import com.naru.backend.model.Post;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByAuthorId(Long authorId);

    List<Post> findByCategoryId(Long categoryId);

    void deleteByAuthorId(Long authorId);

    List<Post> findByCategoryIdIn(List<Long> categoryIds);

    List<Post> findByCategoryIdIsNull();
}