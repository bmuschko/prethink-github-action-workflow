package com.example.orders.service;

import com.example.orders.domain.Customer;
import com.example.orders.domain.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService pricing = new PricingService();

    @Test
    void subtotalSumsLineTotals() {
        List<OrderItem> items = List.of(
                new OrderItem("A", 2, new BigDecimal("10.00")),
                new OrderItem("B", 1, new BigDecimal("5.00")));

        assertEquals(new BigDecimal("25.00"), pricing.subtotal(items));
    }

    @Test
    void goldTierGetsBaseDiscount() {
        Customer gold = new Customer("Ada", "ada@example.com", "GOLD");

        BigDecimal rate = pricing.discountRate(gold, null, new BigDecimal("100"));

        assertEquals(new BigDecimal("0.12"), rate);
    }

    @Test
    void totalAppliesDiscountAndRounds() {
        Customer silver = new Customer("Bea", "bea@example.com", "SILVER");
        List<OrderItem> items = List.of(new OrderItem("A", 1, new BigDecimal("100.00")));

        // SILVER 6% -> 94.00
        assertEquals(new BigDecimal("94.00"), pricing.total(silver, null, items));
    }
}
