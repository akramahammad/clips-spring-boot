package com.project.clips.controller;

import com.project.clips.config.JwtTokenUtil;
import com.project.clips.entity.Clips;
import com.project.clips.entity.TokenRequest;
import com.project.clips.entity.User;
import com.project.clips.repo.ClipsRepo;
import com.project.clips.repo.UserRepo;
import com.project.clips.service.ClipsUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin
public class ClipsController {

    private static final Logger logger= LoggerFactory.getLogger(ClipsController.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClipsRepo clipsRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ClipsUtil clipsUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/user/register")
    public ResponseEntity<Map<String,String>> registerUser(@RequestBody User user){
        logger.info("Inside register user");
        User existingUser=userRepo.findByEmail(user.getEmail());
        Map<String,String> body=new HashMap<>();
        body.put("message","User already exists. Please login");
        if(existingUser!=null){
            return new ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String encodedPassword=passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User newUser=userRepo.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getEmail());
        String token=jwtTokenUtil.generateToken(userDetails);

        body.put("message","User created successfully!");
        body.put("token", token);
        return new ResponseEntity(body,HttpStatus.CREATED);
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody User user){
        logger.info("Inside login");
        String email=user.getEmail();
        String password=user.getPassword();
        Map<String,String> body= new HashMap<>();
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        }
        catch (BadCredentialsException exception){
            logger.error("Invalid credentials");
            body.put("message","Invalid credentials");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }
        catch (Exception exception){
            logger.error("Unable to log in user");
            body.put("message","Unable to log in user");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token=jwtTokenUtil.generateToken(userDetails);

        body.put("message","User logged in successfully");
        body.put("token",token);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<String> checkEmailAvailable(@PathVariable("email") String email){
        logger.info("Inside checkEmailAvailable");
        User user=userRepo.findByEmail(email);
        if (user==null){
            return ResponseEntity.status(HttpStatus.OK).body("Email available for use");
        }
        return  ResponseEntity.status(HttpStatus.OK).body("Email already registered");
    }

    @PostMapping("/user/validate/token")
    public ResponseEntity<String> validateToken(@RequestBody TokenRequest request){
        String token=request.getToken();
        try{
            String email=jwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails=userDetailsService.loadUserByUsername(email);
            if (userDetails!=null){
                boolean validToken=jwtTokenUtil.validateToken(token,userDetails);
                if(validToken) return ResponseEntity.status(200).body("Valid Token");
            }

            return ResponseEntity.status(500).body("Invalid token");
        }
        catch (ExpiredJwtException exception){
            return ResponseEntity.status(500).body("Token Expired");
        }
        catch (Exception exception) {
            logger.error("Error in validating token");
            return ResponseEntity.status(500).body("Error in validating token");
        }
    }

    @GetMapping("/clips")
    public List<Clips> getAllClips( @RequestParam(required = false,value = "offset") Integer offSet){
        logger.info("Inside get all clips");
        Pageable nextPageWithSixClips= PageRequest.of(0,6);
        if(offSet!=null){
            nextPageWithSixClips= PageRequest.of(offSet,6);
        }
        return clipsRepo.findAllByOrderByTimestampDesc(nextPageWithSixClips);
    }

    @GetMapping("/user/clips")
    public ResponseEntity<List<Clips>> getClipsForUser(
                                       @RequestParam(value = "order" ,required = false) String order){
        logger.info("Inside get clips for user");

        User user=clipsUtil.loadUserFromSecurityContext();
        if(order!=null && !order.isEmpty() && order.equals("oldest")){
            return ResponseEntity.status(200)
                    .body(clipsRepo.findByUserIdOrderByTimestampAsc(user.getId()));
        }
        return ResponseEntity.status(200)
                .body(clipsRepo.findByUserIdOrderByTimestampDesc(user.getId()));
    }

    @GetMapping("/clips/{id}")
    public ResponseEntity<String> getClip(@PathVariable("id") String id){
        logger.info("Inside get clips :: {}",id);
        Optional<Clips> clip= clipsRepo.findById(id);
        if(clip.isPresent()){
            return new ResponseEntity(clip,HttpStatus.OK);
        }
        return new ResponseEntity("No clip found",HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @PostMapping("/clips/add")
    public ResponseEntity<Map<String,String>> addClip(
                                  @RequestParam("title") String title,
                                  @RequestParam("clipFileName") String clipFileName,
                                  @RequestParam("clipFile") MultipartFile clipFile,
                                  @RequestParam("screenshotFileName") String screenshotFileName,
                                  @RequestParam("screenshotFile") MultipartFile screenshotFile) throws IOException {
        logger.info("Inside add clip");
        Clips clip= new Clips();
        User user= clipsUtil.loadUserFromSecurityContext();
        clip.setUserId(user.getId());
        clip.setDisplayName(user.getName());
        clip.setTitle(title);
        clip.setClipFileName(clipFileName);
        clip.setClipData(new Binary(BsonBinarySubType.BINARY,clipFile.getBytes()));
        clip.setScreenshotFileName(screenshotFileName);
        clip.setScreenshotData(new Binary(BsonBinarySubType.BINARY,screenshotFile.getBytes()));
        clip.setTimestamp(new Date());

        Clips newClip=clipsRepo.save(clip);
        Map<String,Object> body=new HashMap<>();
        body.put("message","Clip added successfully");
        body.put("clipId",newClip.getId());
        return new ResponseEntity(body,HttpStatus.CREATED);
    }

    @PostMapping("/clip/update")
    public ResponseEntity getClip(@RequestBody Clips clipDetails){
        logger.info("Inside clips update");
        Optional<Clips> clip= clipsRepo.findById(clipDetails.getId());
        if(clip.isPresent()){
            clip.get().setTitle(clipDetails.getTitle());
            clipsRepo.save(clip.get());
            return new ResponseEntity("Clip updated successfully",HttpStatus.OK);
        }
        return new ResponseEntity("No clip found",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/clip/{id}")
    public ResponseEntity deleteClip(@PathVariable String id){
        logger.info("Inside delete clip");
        Optional<Clips> clip= clipsRepo.findById(id);
        if(clip.isPresent()){
            clipsRepo.deleteById(id);
            return new ResponseEntity("Clip deleted successfully",HttpStatus.OK);
        }
        return new ResponseEntity("No clip found",HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
