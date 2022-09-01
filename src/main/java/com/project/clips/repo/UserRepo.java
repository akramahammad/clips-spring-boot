package com.project.clips.repo;

import com.project.clips.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User,String> {
    public User findByEmail(String email);
}
