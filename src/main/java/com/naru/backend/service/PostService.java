package com.naru.backend.service;

import com.naru.backend.dto.PostDTO;
import com.naru.backend.model.Post;
import com.naru.backend.model.User;
import com.naru.backend.repository.PostRepository;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public PostDTO createPost() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Post post = new Post();
        if (authentication != null && authentication.isAuthenticated()) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // User ID를 통해 User 엔티티 조회
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found with ID: " + userPrincipal.getId()));
            post.setUser(user);
        }
        return new PostDTO(postRepository.save(post));
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public Post updatePost(Long id, Post post) {
        post.setPostId(id);
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}