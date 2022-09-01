package com.project.clips.controller;

import com.project.clips.entity.Clips;
import com.project.clips.entity.User;
import com.project.clips.repo.ClipsRepo;
import com.project.clips.repo.UserRepo;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin
public class ClipsController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClipsRepo clipsRepo;

    @PostMapping("/register")
    public ResponseEntity<Map> registerUser(@RequestBody User user){
        User existingUser=userRepo.findByEmail(user.getEmail());
        Map<String,String> body=new HashMap<>();
        body.put("message","User already exists. Please login");
        if(existingUser!=null){
            return new ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User newUser=userRepo.save(user);
        body.put("message","User created successfully!");
        body.put("userId", newUser.getId());
        return new ResponseEntity(body,HttpStatus.CREATED);
    }

    @GetMapping("/clips")
    public List<Clips> getAllClips(){
        return clipsRepo.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/user/{userId}/clips")
    public List<Clips> getClipsForUser(@PathVariable("userId") String userId,
                                       @RequestParam(value = "order" ,required = false) String order){
        if(order!=null && !order.isEmpty() && order.equals("oldest")){
            return clipsRepo.findByUserIdOrderByTimestampAsc(userId);
        }
        return clipsRepo.findByUserIdOrderByTimestampDesc(userId);
    }

    @GetMapping("/clips/{id}")
    public ResponseEntity getClip(@PathVariable("id") String id){
        Optional<Clips> clip= clipsRepo.findById(id);
        if(clip.isPresent()){
            return new ResponseEntity(clip,HttpStatus.OK);
        }
        return new ResponseEntity("No clip found",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/clip/update")
    public ResponseEntity getClip(@RequestBody Clips clipDetails){
        Optional<Clips> clip= clipsRepo.findById(clipDetails.getId());
        if(clip.isPresent()){
            clip.get().setTitle(clipDetails.getTitle());
            clipsRepo.save(clip.get());
            return new ResponseEntity("Clip updated successfully",HttpStatus.OK);
        }
        return new ResponseEntity("No clip found",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/clips/add")
    public ResponseEntity addClip(@RequestParam("userId") String userId,
                                  @RequestParam("displayName") String displayName,
                                  @RequestParam("title") String title,
                                  @RequestParam("clipFileName") String clipFileName,
                                  @RequestParam("clipFile") MultipartFile clipFile,
                                  @RequestParam("screenshotFileName") String screenshotFileName,
                                  @RequestParam("screenshotFile") MultipartFile screenshotFile) throws IOException {
        Clips clip= new Clips();
        clip.setUserId(userId);
        clip.setDisplayName(displayName);
        clip.setTitle(title);
        clip.setClipFileName(clipFileName);
        clip.setClipData(new Binary(BsonBinarySubType.BINARY,clipFile.getBytes()));
        clip.setScreenshotFileName(screenshotFileName);
        clip.setScreenshotData(new Binary(BsonBinarySubType.BINARY,screenshotFile.getBytes()));
        clip.setTimestamp(new Date());

        clipsRepo.save(clip);

        return  new ResponseEntity("Clip added successfully",HttpStatus.CREATED);
    }

}
