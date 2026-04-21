package com.clgmarket.app.service;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.*;
import com.clgmarket.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public BidDto.Response toDto(Bid bid) {
        BidDto.Response dto = new BidDto.Response();
        dto.setId(bid.getId());
        dto.setItemId(bid.getItem().getId());
        dto.setBidder(userService.toDto(bid.getBidder()));
        dto.setAmount(bid.getAmount());
        dto.setCreatedAt(bid.getCreatedAt());
        return dto;
    }

    @Transactional
    public BidDto.Response placeBid(Long itemId, User bidder, BigDecimal amount) {
        // Pessimistic lock via DB transaction
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getListingType() != Item.ListingType.AUCTION)
            throw new RuntimeException("Not an auction item");
        if (item.getStatus() != Item.Status.ACTIVE)
            throw new RuntimeException("Auction has ended");
        if (LocalDateTime.now().isAfter(item.getEndTime()))
            throw new RuntimeException("Auction has expired");
        if (item.getSeller().getId().equals(bidder.getId()))
            throw new RuntimeException("Cannot bid on your own item");
        if (amount.compareTo(item.getCurrentBid() != null ? item.getCurrentBid() : BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Bid must be higher than current bid of ₹" + (item.getCurrentBid() != null ? item.getCurrentBid() : BigDecimal.ZERO));

        User previousHighestBidder = item.getHighestBidder();

        // Anti-sniping
        if (item.isAntiSnipe()) {
            long secondsLeft = java.time.Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds();
            if (secondsLeft <= item.getAntiSnipeThresholdSeconds()) {
                item.setEndTime(item.getEndTime().plusMinutes(item.getAntiSnipeExtendMinutes()));
            }
        }

        item.setCurrentBid(amount);
        item.setHighestBidder(bidder);
        itemRepository.save(item);

        Bid bid = Bid.builder().item(item).bidder(bidder).amount(amount).build();
        bidRepository.save(bid);

        BidDto.Response bidDto = toDto(bid);

        // Broadcast new bid to item subscribers
        BidDto.BidUpdate update = new BidDto.BidUpdate();
        update.setItemId(itemId);
        update.setCurrentBid(item.getCurrentBid());
        update.setHighestBidder(userService.toDto(bidder));
        update.setEndTime(item.getEndTime());
        update.setLatestBid(bidDto);
        messagingTemplate.convertAndSend("/topic/item/" + itemId, update);

        // Notify outbid user
        if (previousHighestBidder != null && !previousHighestBidder.getId().equals(bidder.getId())) {
            messagingTemplate.convertAndSendToUser(
                    previousHighestBidder.getId().toString(),
                    "/queue/outbid",
                    java.util.Map.of("itemId", itemId, "itemTitle", item.getTitle(), "newBid", amount)
            );
        }

        return bidDto;
    }

    public List<BidDto.Response> getBidHistory(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        return bidRepository.findByItemOrderByCreatedAtDesc(item).stream().map(this::toDto).toList();
    }
}
