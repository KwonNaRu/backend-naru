package com.naru.backend.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, PostDTO> lastRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<PostDTO>> debounceResults = new ConcurrentHashMap<>();

    public synchronized PostDTO debounceUpdate(String id, PostDTO postDTO, UserPrincipal userPrincipal) {
        // 마지막 요청 저장 (항상 덮어쓰기)
        lastRequests.put(id, postDTO);

        // 이전 예약된 작업이 있다면 취소
        ScheduledFuture<?> existingTask = scheduler.schedule(() -> {
        }, 0, TimeUnit.SECONDS);
        if (existingTask != null) {
            existingTask.cancel(false);
        }

        // 새로운 요청에 대한 CompletableFuture를 생성하고 저장
        CompletableFuture<PostDTO> future = new CompletableFuture<>();
        debounceResults.put(id, future);

        // 2초 후 마지막 요청을 처리하는 작업 예약
        scheduler.schedule(() -> {
            try {
                // 마지막 요청을 가져와 처리
                PostDTO lastPostDTO = lastRequests.get(id);
                CompletableFuture<PostDTO> lastFuture = debounceResults.get(id);

                if (lastPostDTO != null && lastFuture != null) {
                    // updatePost 실행
                    PostDTO updatedPost = updatePost(id, lastPostDTO, userPrincipal);
                    // 마지막 요청의 future에 결과 반환
                    lastFuture.complete(updatedPost);
                }
            } catch (Exception e) {
                CompletableFuture<PostDTO> lastFuture = debounceResults.get(id);
                if (lastFuture != null) {
                    lastFuture.completeExceptionally(e);
                }
            }
        }, 2, TimeUnit.SECONDS);

        // future 반환하여 동기적으로 결과를 대기하고 반환
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve updated post: " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
    }

    public PostDTO updatePost(String id, PostDTO postDTO, UserPrincipal userPrincipal) {
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
            post.setAuthorId(post.getAuthorId());
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