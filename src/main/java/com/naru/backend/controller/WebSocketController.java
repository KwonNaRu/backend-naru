package com.naru.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import com.naru.backend.dto.PostDTO;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.service.PostService;

@Controller
public class WebSocketController {

    @Autowired
    private PostService postService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/post")
    public void updatePost(Principal principal, @RequestBody PostDTO postDTO) {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        PostDTO savedPost = postService.updatePost(userPrincipal, postDTO.getPostId(), postDTO);
        messagingTemplate.convertAndSend("/topic/posts", savedPost);
    }
}
