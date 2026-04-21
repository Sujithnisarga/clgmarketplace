package com.clgmarket.app.dto;

import com.clgmarket.app.entity.Item;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ItemDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String title;
        @NotBlank private String description;
        @NotNull private Item.Category category;
        @NotNull private Item.ListingType listingType;

        // Fixed
        private BigDecimal price;

        // Auction
        private BigDecimal startingBid;
        private Integer durationMinutes;
        private LocalDateTime endTime;
        private boolean antiSnipe = false;
        private int antiSnipeExtendMinutes = 5;
        private int antiSnipeThresholdSeconds = 60;
    }

    @Data
    public static class Response {
        private Long id;
        private UserDto seller;
        private String title;
        private String description;
        private List<String> images;
        private Item.Category category;
        private Item.ListingType listingType;
        private BigDecimal price;
        private BigDecimal startingBid;
        private BigDecimal currentBid;
        private UserDto highestBidder;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Item.Status status;
        private boolean antiSnipe;
        private int antiSnipeExtendMinutes;
        private int antiSnipeThresholdSeconds;
        private LocalDateTime createdAt;
    }
}
