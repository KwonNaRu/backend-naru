package com.naru.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import com.naru.backend.dto.CategoryDTO;
import com.naru.backend.dto.PostDTO;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.service.CategoryService;
import com.naru.backend.service.PostService;

@Controller
public class WebSocketController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/post")
    public void updatePost(Principal principal, @RequestBody PostDTO postDTO) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        PostDTO savedPost = postService.debounceUpdate(postDTO.getId(), postDTO, userPrincipal);
        messagingTemplate.convertAndSend("/topic/posts", savedPost);
    }

    @MessageMapping("/category")
    public void createCategory(Principal principal, @RequestBody CategoryDTO categoryDTO) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (!userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> "OWNER".equals(auth.getAuthority()))) {
            throw new AccessDeniedException("OWNER 권한이 필요합니다.");
        }

        CategoryDTO savedCategory = categoryService.createCategory(categoryDTO);
        messagingTemplate.convertAndSend("/topic/create/category", savedCategory);
    }
}
