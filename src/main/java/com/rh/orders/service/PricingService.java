package com.rh.orders.service;

import com.rh.orders.model.LoyaltyTier;
import com.rh.orders.model.OrderItem;
import com.rh.orders.model.OrderRequest;
import com.rh.orders.model.OrderResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

@ApplicationScoped
public class PricingService {

    private static final BigDecimal VOLUME_THRESHOLD_100 = new BigDecimal("100");
    private static final BigDecimal VOLUME_THRESHOLD_500 = new BigDecimal("500");
    private static final BigDecimal VOLUME_THRESHOLD_1000 = new BigDecimal("1000");

    private static final BigDecimal VOLUME_RATE_3 = new BigDecimal("0.03");
    private static final BigDecimal VOLUME_RATE_5 = new BigDecimal("0.05");
    private static final BigDecimal VOLUME_RATE_8 = new BigDecimal("0.08");

    private record PromoDefinition(BigDecimal discountRate, BigDecimal flatAmount,
                                   LocalDate expiry, BigDecimal minOrder) {}

    private static final Map<String, PromoDefinition> PROMO_CODES = Map.of(
            "SUMMER25", new PromoDefinition(new BigDecimal("0.25"), null,
                    LocalDate.of(2026, 9, 30), new BigDecimal("50")),
            "SAVE10", new PromoDefinition(new BigDecimal("0.10"), null,
                    LocalDate.of(2026, 12, 31), null),
            "FLAT20", new PromoDefinition(null, new BigDecimal("20"),
                    LocalDate.of(2026, 10, 15), new BigDecimal("75"))
    );

    public OrderResponse calculatePrice(OrderRequest request) {
        BigDecimal subtotal = calculateSubtotal(request);
        BigDecimal loyaltyDiscount = calculateLoyaltyDiscount(subtotal, request.getLoyaltyTier());
        BigDecimal volumeDiscount = calculateVolumeDiscount(subtotal);
        BigDecimal promoDiscount = calculatePromoDiscount(subtotal, request.getPromoCode());

        BigDecimal totalDiscount = loyaltyDiscount.add(volumeDiscount).add(promoDiscount);
        if (totalDiscount.compareTo(subtotal) > 0) {
            totalDiscount = subtotal;
        }
        BigDecimal finalPrice = subtotal.subtract(totalDiscount);

        OrderResponse response = new OrderResponse();
        response.setSubtotal(subtotal);
        response.setLoyaltyDiscount(loyaltyDiscount);
        response.setVolumeDiscount(volumeDiscount);
        response.setPromoDiscount(promoDiscount);
        response.setTotalDiscount(totalDiscount);
        response.setFinalPrice(finalPrice);
        return response;
    }

    private BigDecimal calculateSubtotal(OrderRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : request.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLoyaltyDiscount(BigDecimal subtotal, LoyaltyTier tier) {
        return subtotal.multiply(tier.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolumeDiscount(BigDecimal subtotal) {
        BigDecimal rate;
        if (subtotal.compareTo(VOLUME_THRESHOLD_1000) >= 0) {
            rate = VOLUME_RATE_8;
        } else if (subtotal.compareTo(VOLUME_THRESHOLD_500) >= 0) {
            rate = VOLUME_RATE_5;
        } else if (subtotal.compareTo(VOLUME_THRESHOLD_100) >= 0) {
            rate = VOLUME_RATE_3;
        } else {
            return BigDecimal.ZERO.setScale(2);
        }
        return subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePromoDiscount(BigDecimal subtotal, String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return BigDecimal.ZERO.setScale(2);
        }

        PromoDefinition promo = PROMO_CODES.get(promoCode.toUpperCase());
        if (promo == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (LocalDate.now().isAfter(promo.expiry())) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (promo.minOrder() != null && subtotal.compareTo(promo.minOrder()) < 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (promo.flatAmount() != null) {
            return promo.flatAmount().setScale(2);
        }

        return subtotal.multiply(promo.discountRate()).setScale(2, RoundingMode.HALF_UP);
    }
}
