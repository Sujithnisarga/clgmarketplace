package com.clgmarket.app.controller;

import com.clgmarket.app.entity.*;
import com.clgmarket.app.repository.ItemRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ItemRepository itemRepository;

    @Value("${stripe.secret.key}")
    private String stripeKey;

    @Value("${app.cors.allowed-origins}")
    private String clientUrl;

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Long> body) throws Exception {

        Stripe.apiKey = stripeKey;
        Long itemId = body.get("itemId");
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

        BigDecimal amount = item.getListingType() == Item.ListingType.FIXED ? item.getPrice() : item.getCurrentBid();
        String origin = clientUrl.split(",")[0].trim();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(origin + "/payment-success.html?itemId=" + itemId)
                .setCancelUrl(origin + "/item.html?id=" + itemId)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getTitle())
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }
}
