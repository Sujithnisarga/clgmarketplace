package com.clgmarket.app.controller;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.User;
import com.clgmarket.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatDto.Response> sendMessage(
            @AuthenticationPrincipal User user,
            @RequestBody ChatDto.SendRequest req) {
        return ResponseEntity.ok(chatService.sendMessage(user, req));
    }

    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<List<ChatDto.Response>> getConversation(
            @AuthenticationPrincipal User user,
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(chatService.getConversation(user, partnerId));
    }

    @GetMapping("/partners")
    public ResponseEntity<List<UserDto>> getChatPartners(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.getChatPartners(user));
    }
}
