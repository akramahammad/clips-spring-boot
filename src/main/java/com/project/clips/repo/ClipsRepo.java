package com.project.clips.repo;

import com.project.clips.entity.Clips;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClipsRepo extends MongoRepository<Clips,String> {
    public List<Clips> findByUserIdOrderByTimestampDesc(String userId);
    public List<Clips> findByUserIdOrderByTimestampAsc(String userId);
    public List<Clips> findAllByOrderByTimestampDesc();

}
