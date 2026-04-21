package com.clgmarket.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidDto {

    @Data
    public static class PlaceRequest {
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
    }

    @Data
    public static class Response {
        private Long id;
        private Long itemId;
        private UserDto bidder;
        private BigDecimal amount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class BidUpdate {
        private Long itemId;
        private BigDecimal currentBid;
        private UserDto highestBidder;
        private LocalDateTime endTime;
        private Response latestBid;
    }
}
