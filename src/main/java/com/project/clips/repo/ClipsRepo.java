package com.project.clips.repo;

import com.project.clips.entity.Clips;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClipsRepo extends MongoRepository<Clips,String> {
     List<Clips> findByUserIdOrderByTimestampDesc(String userId);
     List<Clips> findByUserIdOrderByTimestampAsc(String userId);
     List<Clips> findAllByOrderByTimestampDesc(Pageable pageable);

}
