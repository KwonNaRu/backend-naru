package com.naru.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import com.naru.backend.dto.CategoryDto;
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

    @MessageMapping("/create/post")
    public void createPost(Principal principal) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        PostDTO savedPost = postService.createPost(userPrincipal);
        messagingTemplate.convertAndSend("/topic/create/post", savedPost);
    }

    @MessageMapping("/category")
    public void createCategory(Principal principal, @RequestBody CategoryDto categoryDTO) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        CategoryDto savedCategory = categoryService.createCategory(categoryDTO, userPrincipal);
        messagingTemplate.convertAndSend("/topic/create/category", savedCategory);
    }
}
