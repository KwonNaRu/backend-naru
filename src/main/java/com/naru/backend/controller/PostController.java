package com.naru.backend.controller;

import com.naru.backend.dto.PostDTO;
import com.naru.backend.model.Post;
import com.naru.backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // 모든 글 조회
    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postService.getAllPosts();
    }

    // 글 생성
    @PostMapping
    public PostDTO createPost() {
        return postService.createPost();
    }

    // 특정 글 조회
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable String id) {
        return postService.getPostById(id); // Post 엔티티에 댓글이 포함되어 반환됨
    }

    // 글 삭제
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable String id) {
        postService.deletePost(id);
    }
}