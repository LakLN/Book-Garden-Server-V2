package com.example.bookgarden.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
        User user = null;
        if(userRepository.findByEmailAndIsActiveIsTrue(emailOrPhone).isPresent()) {
            user = userRepository.findByEmailAndIsActiveIsTrue(emailOrPhone)
                    .orElseThrow(() -> new UsernameNotFoundException("user is not found"));
        }
        return new UserDetail(user);
    }

    public UserDetails loadUserByUserId(String id){
        User user = userRepository.findByIdAndIsActiveIsTrue(id)
                .orElseThrow(()->new UsernameNotFoundException("user is not found"));
        return new UserDetail(user);
    }
}
