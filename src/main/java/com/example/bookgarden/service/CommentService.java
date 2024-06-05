package com.example.bookgarden.service;

import com.example.bookgarden.dto.CommentDTO;
import com.example.bookgarden.dto.UserPostDTO;
import com.example.bookgarden.entity.Comment;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.CommentRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    public CommentDTO convertCommentToDTO(Comment comment){
        ModelMapper modelMapper = new ModelMapper();
        CommentDTO commentDTO = modelMapper.map(comment, CommentDTO.class);
        Optional<User> optionalUser = userRepository.findById(comment.getUser().toString());
        if(optionalUser.isPresent()){
            UserPostDTO userPostDTO = modelMapper.map(optionalUser.get(), UserPostDTO.class);
            commentDTO.setUser(userPostDTO);
        }
        List<CommentDTO> replyDTOs = convertRepliesToDTOs(comment.getReplies());
        commentDTO.setReplies(replyDTOs);
        return commentDTO;
    }

    private List<CommentDTO> convertRepliesToDTOs(List<ObjectId> replyIds) {
        List<CommentDTO> replyDTOs = new ArrayList<>();
        for (ObjectId replyId : replyIds) {
            Optional<Comment> optionalReply = commentRepository.findById(replyId);
            optionalReply.ifPresent(reply -> replyDTOs.add(convertCommentToDTO(reply)));
        }
        return replyDTOs;
    }
}
