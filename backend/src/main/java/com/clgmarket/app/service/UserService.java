package com.clgmarket.app.service;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.*;
import com.clgmarket.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final ReviewRepository reviewRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCollege(user.getCollege());
        dto.setBio(user.getBio());
        dto.setAvatar(user.getAvatar());
        dto.setRating(user.getRating());
        dto.setRatingCount(user.getRatingCount());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public UserDto updateProfile(User user, String name, String bio, String college, MultipartFile avatar) throws IOException {
        if (name != null) user.setName(name);
        if (bio != null) user.setBio(bio);
        if (college != null) user.setCollege(college);
        if (avatar != null && !avatar.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
            Path path = Paths.get(uploadDir, filename);
            Files.createDirectories(path.getParent());
            Files.write(path, avatar.getBytes());
            user.setAvatar("/uploads/" + filename);
        }
        return toDto(userRepository.save(user));
    }

    public Map<String, Object> getDashboard(User user) {
        List<ItemDto.Response> listings = itemRepository.findBySellerOrderByCreatedAtDesc(user)
                .stream().map(this::itemToDto).toList();
        List<ItemDto.Response> biddedItems = bidRepository.findDistinctItemsByBidder(user)
                .stream().map(this::itemToDto).toList();
        List<ItemDto.Response> wonAuctions = itemRepository.findByHighestBidderAndStatus(user, Item.Status.SOLD)
                .stream().map(this::itemToDto).toList();
        return Map.of("listings", listings, "biddedItems", biddedItems, "wonAuctions", wonAuctions);
    }

    public Map<String, Object> getPublicProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<ItemDto.Response> listings = itemRepository.findBySellerOrderByCreatedAtDesc(user)
                .stream().map(this::itemToDto).toList();
        List<ReviewDto> reviews = reviewRepository.findByRevieweeOrderByCreatedAtDesc(user)
                .stream().map(this::toReviewDto).toList();
        return Map.of("user", toDto(user), "listings", listings, "reviews", reviews);
    }

    public ReviewDto toReviewDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setId(r.getId());
        dto.setReviewer(toDto(r.getReviewer()));
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    // Minimal item DTO conversion to avoid circular dependency with ItemService
    private ItemDto.Response itemToDto(Item item) {
        ItemDto.Response dto = new ItemDto.Response();
        dto.setId(item.getId());
        dto.setSeller(toDto(item.getSeller()));
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setImages(item.getImages());
        dto.setCategory(item.getCategory());
        dto.setListingType(item.getListingType());
        dto.setPrice(item.getPrice());
        dto.setStartingBid(item.getStartingBid());
        dto.setCurrentBid(item.getCurrentBid());
        dto.setHighestBidder(toDto(item.getHighestBidder()));
        dto.setStartTime(item.getStartTime());
        dto.setEndTime(item.getEndTime());
        dto.setStatus(item.getStatus());
        dto.setAntiSnipe(item.isAntiSnipe());
        dto.setAntiSnipeExtendMinutes(item.getAntiSnipeExtendMinutes());
        dto.setAntiSnipeThresholdSeconds(item.getAntiSnipeThresholdSeconds());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }
}
