package com.clgmarket.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class ChatDto {

    @Data
    public static class SendRequest {
        private Long receiverId;
        private Long itemId;
        private String content;
    }

    @Data
    public static class Response {
        private Long id;
        private UserDto sender;
        private UserDto receiver;
        private Long itemId;
        private String content;
        private boolean read;
        private LocalDateTime createdAt;
    }
}
