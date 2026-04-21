package com.clgmarket.app.controller;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.User;
import com.clgmarket.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.toDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@AuthenticationPrincipal User user,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) String bio,
                                             @RequestParam(required = false) String college,
                                             @RequestParam(required = false) MultipartFile avatar) throws IOException {
        return ResponseEntity.ok(userService.updateProfile(user, name, bio, college, avatar));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getDashboard(user));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<?> addReview(@AuthenticationPrincipal User user,
                                        @PathVariable Long id,
                                        @RequestParam Long itemId,
                                        @RequestParam int rating,
                                        @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(reviewService.addReview(user, id, itemId, rating, comment));
    }
}
