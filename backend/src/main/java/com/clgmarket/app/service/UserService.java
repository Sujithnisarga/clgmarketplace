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
        List<Item> listings = itemRepository.findBySellerOrderByCreatedAtDesc(user);
        List<Item> biddedItems = bidRepository.findDistinctItemsByBidder(user);
        List<Item> wonAuctions = itemRepository.findByHighestBidderAndStatus(user, Item.Status.SOLD);
        return Map.of("listings", listings, "biddedItems", biddedItems, "wonAuctions", wonAuctions);
    }

    public Map<String, Object> getPublicProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Item> listings = itemRepository.findBySellerOrderByCreatedAtDesc(user);
        List<Review> reviews = reviewRepository.findByRevieweeOrderByCreatedAtDesc(user);
        return Map.of("user", toDto(user), "listings", listings, "reviews", reviews);
    }
}
