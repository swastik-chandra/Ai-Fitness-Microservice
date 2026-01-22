package com.fitness.userservice.Controller;

import com.fitness.userservice.Service.UserService;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@NoArgsConstructor // koi bhi use kar sakte hai NoArgsConstructor ya fir autowired
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserProfile(userId));

    }
    @PostMapping ("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(userService.register(request));

    }
    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> ValidateUser(@PathVariable String userId){
        return ResponseEntity.ok(userService.existByUserId(userId));

    }
}
