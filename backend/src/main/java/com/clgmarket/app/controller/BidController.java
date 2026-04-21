package com.clgmarket.app.controller;

import com.clgmarket.app.dto.BidDto;
import com.clgmarket.app.entity.User;
import com.clgmarket.app.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping("/{itemId}")
    public ResponseEntity<BidDto.Response> placeBid(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody BidDto.PlaceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bidService.placeBid(itemId, user, req.getAmount()));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<List<BidDto.Response>> getBidHistory(@PathVariable Long itemId) {
        return ResponseEntity.ok(bidService.getBidHistory(itemId));
    }
}
