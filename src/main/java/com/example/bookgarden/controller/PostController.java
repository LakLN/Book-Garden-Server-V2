package com.example.bookgarden.controller;

import com.example.bookgarden.dto.CommentRequestDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.PostCreateRequestDTO;
import com.example.bookgarden.entity.Post;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
    //Edit Post
    @PutMapping("/{postId}")
    public  ResponseEntity<GenericResponse> editPost(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String postId,
                                                     @Valid @RequestBody PostCreateRequestDTO editPostRequestDTO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : errors) {
                String errorMessage = error.getDefaultMessage();
                errorMessages.add(errorMessage);
            }
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.editPost(userId, postId, editPostRequestDTO);
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
