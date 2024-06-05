package com.example.bookgarden.controller;

import com.example.bookgarden.dto.CommentRequestDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.PostCreateRequestDTO;
import com.example.bookgarden.entity.Post;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createPost(@RequestHeader("Authorization") String authorizationHeader, @RequestBody PostCreateRequestDTO createPostRequestDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.createPost(userId, createPostRequestDTO);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAllApprovedPosts(){
        return postService.getAllApprovedPosts();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<GenericResponse> getPostById(@PathVariable String postId){
        return postService.getPostById(postId);
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<GenericResponse> commentPost(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String postId,
                                                       @RequestBody CommentRequestDTO commentRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.commentPost(userId, postId, commentRequestDTO);
    }
    @PostMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<GenericResponse> replyPostComment(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String postId,
                                                            @PathVariable String commentId, @RequestBody CommentRequestDTO commentRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.replyPostComment(userId, postId, commentId, commentRequestDTO);
    }
}
