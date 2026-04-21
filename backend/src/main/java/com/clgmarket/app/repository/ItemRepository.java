package com.clgmarket.app.repository;

import com.clgmarket.app.entity.Item;
import com.clgmarket.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    List<Item> findBySellerOrderByCreatedAtDesc(User seller);

    @Query("SELECT i FROM Item i WHERE i.listingType = 'AUCTION' AND i.status = 'ACTIVE' AND i.endTime <= :now")
    List<Item> findExpiredAuctions(LocalDateTime now);

    List<Item> findByHighestBidderAndStatus(User highestBidder, Item.Status status);
}
