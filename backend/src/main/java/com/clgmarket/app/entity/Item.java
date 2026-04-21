package com.clgmarket.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingType listingType;

    // Fixed price
    private BigDecimal price;

    // Auction fields
    private BigDecimal startingBid;
    private BigDecimal currentBid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bidder_id")
    private User highestBidder;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Builder.Default
    private boolean antiSnipe = false;

    @Builder.Default
    private int antiSnipeExtendMinutes = 5;

    @Builder.Default
    private int antiSnipeThresholdSeconds = 60;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Category { BOOKS, ELECTRONICS, CLOTHING, FURNITURE, SPORTS, OTHER }
    public enum ListingType { FIXED, AUCTION }
    public enum Status { ACTIVE, ENDED, SOLD }
}
