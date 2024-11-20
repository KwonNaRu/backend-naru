package com.naru.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naru.backend.dto.PostDTO;
import com.naru.backend.model.Post;
import com.naru.backend.model.User;
import com.naru.backend.repository.CategoryRepository;
import com.naru.backend.repository.PostRepository;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.util.SecurityUtil;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtil securityUtil;

    public List<PostDTO> getAllPosts() {
        return postRepository.findAll().stream().map(PostDTO::new).toList();
    }

    public PostDTO createPost() {

        User user = securityUtil.getAuthenticatedUser();

        Post post = new Post();
        post.setAuthorId(user.getUserId());
        post.setAuthor(user.getUsername());
        return new PostDTO(postRepository.save(post));
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public PostDTO updatePost(UserPrincipal userPrincipal, String id, PostDTO postDTO) {
        try {
            // User ID를 통해 User 엔티티 조회
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(
                            () -> new UsernameNotFoundException(
                                    "User not found with ID: " + userPrincipal.getId()));
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            // 작성자 검증
            if (!post.getAuthorId().equals(user.getUserId())) {
                throw new RuntimeException("Not authorized to update this post");
            }
            post.setId(id);
            post.setTitle(postDTO.getTitle());
            post.setContent(postDTO.getContent());
            post.setCategoryId(postDTO.getCategoryId());
            return new PostDTO(postRepository.save(post));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update post: " + e.getMessage());
        }
    }

    public void deletePost(String id) {
        postRepository.deleteById(id);
    }
}