package com.clgmarket.app.controller;

import com.clgmarket.app.dto.ItemDto;
import com.clgmarket.app.entity.User;
import com.clgmarket.app.repository.ItemRepository;
import com.clgmarket.app.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemRepository itemRepository;

    @GetMapping
    public ResponseEntity<List<ItemDto.Response>> browse(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String listingType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(itemService.searchItems(search, category, listingType, minPrice, maxPrice, status, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto.Response> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.toDto(
                itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"))
        ));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemDto.Response> createItem(
            @AuthenticationPrincipal User user,
            @RequestPart("data") ItemDto.CreateRequest req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(user, req, images));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemDto.Response> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestPart("data") ItemDto.CreateRequest req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        return ResponseEntity.ok(itemService.updateItem(id, user, req, images));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@AuthenticationPrincipal User user, @PathVariable Long id) {
        itemService.deleteItem(id, user);
        return ResponseEntity.noContent().build();
    }
}
