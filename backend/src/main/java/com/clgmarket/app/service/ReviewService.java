package com.clgmarket.app.service;

import com.clgmarket.app.entity.*;
import com.clgmarket.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    public Review addReview(User reviewer, Long revieweeId, Long itemId, int rating, String comment) {
        if (reviewRepository.existsByReviewerAndItemId(reviewer, itemId))
            throw new RuntimeException("Already reviewed this item");

        User reviewee = userRepository.findById(revieweeId).orElseThrow(() -> new RuntimeException("User not found"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

        Review review = Review.builder()
                .reviewer(reviewer).reviewee(reviewee).item(item).rating(rating).comment(comment).build();
        reviewRepository.save(review);

        List<Review> all = reviewRepository.findByRevieweeOrderByCreatedAtDesc(reviewee);
        double avg = all.stream().mapToInt(Review::getRating).average().orElse(0);
        reviewee.setRating(avg);
        reviewee.setRatingCount(all.size());
        userRepository.save(reviewee);

        return review;
    }
}
