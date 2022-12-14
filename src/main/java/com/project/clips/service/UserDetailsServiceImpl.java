package com.project.clips.service;

import com.project.clips.entity.User;
import com.project.clips.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepo.findByEmail(username);
        if (user==null){
            throw new UsernameNotFoundException("User does not exists");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),new ArrayList<>());
    }
}
