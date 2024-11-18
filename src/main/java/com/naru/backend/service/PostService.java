package com.naru.backend.service;

import com.naru.backend.dto.PostDTO;
import com.naru.backend.model.Category;
import com.naru.backend.model.Post;
import com.naru.backend.model.User;
import com.naru.backend.repository.CategoryRepository;
import com.naru.backend.repository.PostRepository;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.util.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SecurityUtil securityUtil;

    public List<PostDTO> getAllPosts() {
        return postRepository.findAll().stream().map(PostDTO::new).toList();
    }

    public PostDTO createPost() {

        User user = securityUtil.getAuthenticatedUser();

        Post post = new Post();
        post.setUser(user);
        return new PostDTO(postRepository.save(post));
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    @Async
    public PostDTO updatePost(UserPrincipal userPrincipal, Long id, PostDTO postDTO) {
        try {
            // User ID를 통해 User 엔티티 조회
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(
                            () -> new UsernameNotFoundException(
                                    "User not found with ID: " + userPrincipal.getId()));
            Category category = postDTO.getCategoryId() != null
                    ? categoryRepository.findById(postDTO.getCategoryId()).orElse(null)
                    : null;
            Post post = new Post();
            post.setPostId(id);
            post.setUser(user);
            post.setTitle(postDTO.getTitle());
            post.setContent(postDTO.getContent());
            post.setCategory(category);
            return new PostDTO(postRepository.save(post));
        } catch (

        Exception e) {
            throw new RuntimeException("Failed to update post: " + e.getMessage());
        }
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}