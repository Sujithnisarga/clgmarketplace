package com.clgmarket.app.service;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.*;
import com.clgmarket.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatDto.Response toDto(ChatMessage msg) {
        ChatDto.Response dto = new ChatDto.Response();
        dto.setId(msg.getId());
        dto.setSender(userService.toDto(msg.getSender()));
        dto.setReceiver(userService.toDto(msg.getReceiver()));
        dto.setItemId(msg.getItem() != null ? msg.getItem().getId() : null);
        dto.setContent(msg.getContent());
        dto.setRead(msg.isRead());
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }

    public ChatDto.Response sendMessage(User sender, ChatDto.SendRequest req) {
        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        Item item = req.getItemId() != null
                ? itemRepository.findById(req.getItemId()).orElse(null) : null;

        ChatMessage msg = ChatMessage.builder()
                .sender(sender).receiver(receiver).item(item).content(req.getContent()).build();
        chatMessageRepository.save(msg);

        ChatDto.Response dto = toDto(msg);
        messagingTemplate.convertAndSendToUser(receiver.getId().toString(), "/queue/messages", dto);
        return dto;
    }

    public List<ChatDto.Response> getConversation(User user, Long partnerId) {
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findConversation(user, partner).stream().map(this::toDto).toList();
    }

    public List<UserDto> getChatPartners(User user) {
        return chatMessageRepository.findChatPartners(user).stream().map(userService::toDto).toList();
    }
}
