package com.clgmarket.app.repository;

import com.clgmarket.app.entity.Review;
import com.clgmarket.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeOrderByCreatedAtDesc(User reviewee);
    boolean existsByReviewerAndItemId(User reviewer, Long itemId);
}
