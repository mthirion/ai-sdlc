package com.rh.orders;

import com.rh.orders.model.LoyaltyTier;
import com.rh.orders.model.OrderItem;
import com.rh.orders.model.OrderRequest;
import com.rh.orders.model.OrderResponse;
import com.rh.orders.service.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingServiceTest {

    private final PricingService service = new PricingService();

    @Test
    void finalPriceNeverNegative() {
        // PLATINUM(15%) + volume(3%) + SAVE10(10%) on $100 = $28 off
        OrderRequest request = new OrderRequest();
        request.setCustomerId("C1");
        request.setLoyaltyTier(LoyaltyTier.PLATINUM);
        request.setPromoCode("SAVE10");

        OrderItem item = new OrderItem();
        item.setProductId("P1");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100.00"));
        request.setItems(List.of(item));

        OrderResponse response = service.calculatePrice(request);

        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(new BigDecimal("15.00"), response.getLoyaltyDiscount());
        assertEquals(new BigDecimal("3.00"), response.getVolumeDiscount());
        assertEquals(new BigDecimal("10.00"), response.getPromoDiscount());
        assertEquals(new BigDecimal("28.00"), response.getTotalDiscount());
        assertEquals(new BigDecimal("72.00"), response.getFinalPrice());
        assertTrue(response.getFinalPrice().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void volumeDiscountTiers() {
        assertEquals(bd("0.00"), priceFor(bd("99.99"), LoyaltyTier.NONE).getVolumeDiscount());
        assertEquals(bd("3.00"), priceFor(bd("100.00"), LoyaltyTier.NONE).getVolumeDiscount());
        assertEquals(bd("15.00"), priceFor(bd("499.99"), LoyaltyTier.NONE).getVolumeDiscount());
        assertEquals(bd("25.00"), priceFor(bd("500.00"), LoyaltyTier.NONE).getVolumeDiscount());
        assertEquals(bd("50.00"), priceFor(bd("999.99"), LoyaltyTier.NONE).getVolumeDiscount());
        assertEquals(bd("80.00"), priceFor(bd("1000.00"), LoyaltyTier.NONE).getVolumeDiscount());
    }

    private OrderResponse priceFor(BigDecimal unitPrice, LoyaltyTier tier) {
        OrderRequest request = new OrderRequest();
        request.setCustomerId("test");
        request.setLoyaltyTier(tier);
        OrderItem item = new OrderItem();
        item.setProductId("P1");
        item.setQuantity(1);
        item.setUnitPrice(unitPrice);
        request.setItems(List.of(item));
        return service.calculatePrice(request);
    }

    private static BigDecimal bd(String val) {
        return new BigDecimal(val);
    }
}
