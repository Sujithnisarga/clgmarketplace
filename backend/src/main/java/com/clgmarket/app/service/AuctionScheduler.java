package com.clgmarket.app.service;

import com.clgmarket.app.entity.Item;
import com.clgmarket.app.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final ItemRepository itemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Scheduled(fixedDelay = 30000) // every 30 seconds
    @Transactional
    public void processExpiredAuctions() {
        List<Item> expired = itemRepository.findExpiredAuctions(LocalDateTime.now());
        for (Item item : expired) {
            item.setStatus(item.getHighestBidder() != null ? Item.Status.SOLD : Item.Status.ENDED);
            itemRepository.save(item);

            messagingTemplate.convertAndSend("/topic/item/" + item.getId(), Map.of(
                    "type", "AUCTION_ENDED",
                    "itemId", item.getId(),
                    "status", item.getStatus().name(),
                    "finalBid", item.getCurrentBid() != null ? item.getCurrentBid() : 0,
                    "winner", item.getHighestBidder() != null ? userService.toDto(item.getHighestBidder()) : null
            ));
            messagingTemplate.convertAndSend("/topic/auctions", Map.of(
                    "type", "STATUS_CHANGE",
                    "itemId", item.getId(),
                    "status", item.getStatus().name()
            ));
            log.info("Auction ended for item {}: status={}", item.getId(), item.getStatus());
        }
    }
}
