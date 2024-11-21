package com.naru.backend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naru.backend.dto.PostDTO;
import com.naru.backend.model.Post;
import com.naru.backend.model.User;
import com.naru.backend.repository.PostRepository;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.util.SecurityUtil;

import jakarta.annotation.PreDestroy;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtil securityUtil;

    private Map<String, ScheduledFuture<?>> debounceTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<PostDTO> getAllPosts() {
        return postRepository.findByCategoryIdIsNull().stream().map(PostDTO::new).toList();
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

    public PostDTO updatePostWithDebounce(UserPrincipal userPrincipal, String id, PostDTO postDTO) {
        // 이전 예약된 작업이 있다면 취소
        ScheduledFuture<?> existingTask = debounceTasks.get(id);
        if (existingTask != null) {
            existingTask.cancel(false);
        }

        // 2초 후에 실행될 새로운 작업 예약
        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
            try {
                updatePost(userPrincipal, id, postDTO);
                debounceTasks.remove(id);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update post: " + e.getMessage());
            }
        }, 2, TimeUnit.SECONDS);

        debounceTasks.put(id, newTask);
        return postDTO; // 즉시 응답 반환
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
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