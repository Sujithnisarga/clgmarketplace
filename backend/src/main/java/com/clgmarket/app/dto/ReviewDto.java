package com.clgmarket.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private UserDto reviewer;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
