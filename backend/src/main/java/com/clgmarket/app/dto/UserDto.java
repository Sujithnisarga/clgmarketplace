package com.clgmarket.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String college;
    private String bio;
    private String avatar;
    private double rating;
    private int ratingCount;
    private LocalDateTime createdAt;
}
