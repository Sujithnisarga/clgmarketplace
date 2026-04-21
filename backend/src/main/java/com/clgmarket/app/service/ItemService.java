package com.clgmarket.app.service;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.*;
import com.clgmarket.app.repository.ItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ItemDto.Response toDto(Item item) {
        ItemDto.Response dto = new ItemDto.Response();
        dto.setId(item.getId());
        dto.setSeller(userService.toDto(item.getSeller()));
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setImages(item.getImages());
        dto.setCategory(item.getCategory());
        dto.setListingType(item.getListingType());
        dto.setPrice(item.getPrice());
        dto.setStartingBid(item.getStartingBid());
        dto.setCurrentBid(item.getCurrentBid());
        dto.setHighestBidder(userService.toDto(item.getHighestBidder()));
        dto.setStartTime(item.getStartTime());
        dto.setEndTime(item.getEndTime());
        dto.setStatus(item.getStatus());
        dto.setAntiSnipe(item.isAntiSnipe());
        dto.setAntiSnipeExtendMinutes(item.getAntiSnipeExtendMinutes());
        dto.setAntiSnipeThresholdSeconds(item.getAntiSnipeThresholdSeconds());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public ItemDto.Response createItem(User seller, ItemDto.CreateRequest req, List<MultipartFile> images) throws IOException {
        List<String> imageUrls = saveImages(images);
        Item.ItemBuilder builder = Item.builder()
                .seller(seller)
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .listingType(req.getListingType())
                .images(imageUrls)
                .status(Item.Status.ACTIVE);

        if (req.getListingType() == Item.ListingType.FIXED) {
            builder.price(req.getPrice());
        } else {
            LocalDateTime end = req.getEndTime() != null
                    ? req.getEndTime()
                    : LocalDateTime.now().plusMinutes(req.getDurationMinutes());
            builder.startingBid(req.getStartingBid())
                    .currentBid(req.getStartingBid())
                    .startTime(LocalDateTime.now())
                    .endTime(end)
                    .antiSnipe(req.isAntiSnipe())
                    .antiSnipeExtendMinutes(req.getAntiSnipeExtendMinutes())
                    .antiSnipeThresholdSeconds(req.getAntiSnipeThresholdSeconds());
        }
        return toDto(itemRepository.save(builder.build()));
    }

    public List<ItemDto.Response> searchItems(String search, String category, String listingType,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               String status, String sort) {
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank())
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                ));
            if (category != null) predicates.add(cb.equal(root.get("category"), Item.Category.valueOf(category)));
            if (listingType != null) predicates.add(cb.equal(root.get("listingType"), Item.ListingType.valueOf(listingType)));
            if (status != null) predicates.add(cb.equal(root.get("status"), Item.Status.valueOf(status)));
            else predicates.add(cb.equal(root.get("status"), Item.Status.ACTIVE));
            if (minPrice != null) predicates.add(cb.or(
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice),
                    cb.greaterThanOrEqualTo(root.get("currentBid"), minPrice)
            ));
            if (maxPrice != null) predicates.add(cb.or(
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice),
                    cb.lessThanOrEqualTo(root.get("currentBid"), maxPrice)
            ));
            if (sort != null && sort.equals("ending_soon"))
                query.orderBy(cb.asc(root.get("endTime")));
            else if (sort != null && sort.equals("price_asc"))
                query.orderBy(cb.asc(root.get("price")));
            else if (sort != null && sort.equals("price_desc"))
                query.orderBy(cb.desc(root.get("price")));
            else
                query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return itemRepository.findAll(spec).stream().map(this::toDto).toList();
    }

    public ItemDto.Response updateItem(Long itemId, User user, ItemDto.CreateRequest req, List<MultipartFile> images) throws IOException {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getSeller().getId().equals(user.getId())) throw new RuntimeException("Forbidden");
        if (item.getListingType() == Item.ListingType.AUCTION &&
                item.getCurrentBid() != null && item.getStartingBid() != null &&
                item.getCurrentBid().compareTo(item.getStartingBid()) > 0)
            throw new RuntimeException("Cannot edit auction with active bids");

        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setCategory(req.getCategory());
        if (req.getPrice() != null) item.setPrice(req.getPrice());
        if (images != null && !images.isEmpty()) item.setImages(saveImages(images));
        return toDto(itemRepository.save(item));
    }

    public void deleteItem(Long itemId, User user) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getSeller().getId().equals(user.getId())) throw new RuntimeException("Forbidden");
        itemRepository.delete(item);
    }

    private List<String> saveImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null) return urls;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, filename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            urls.add("/uploads/" + filename);
        }
        return urls;
    }
}
