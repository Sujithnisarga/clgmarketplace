package com.clgmarket.app.repository;

import com.clgmarket.app.entity.Bid;
import com.clgmarket.app.entity.Item;
import com.clgmarket.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByItemOrderByCreatedAtDesc(Item item);

    @Query("SELECT DISTINCT b.item FROM Bid b WHERE b.bidder = :bidder")
    List<Item> findDistinctItemsByBidder(User bidder);
}
