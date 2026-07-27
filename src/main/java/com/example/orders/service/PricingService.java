package com.example.orders.service;

import com.example.orders.domain.Customer;
import com.example.orders.domain.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pricing rules. Intentionally branchy so complexity/test-gap analysis has
 * something to chew on: tier discounts, coupon stacking, and volume breaks.
 */
@Service
public class PricingService {

    public BigDecimal subtotal(List<OrderItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(item.lineTotal());
        }
        return sum;
    }

    /**
     * High cyclomatic complexity by design (tier x coupon x volume).
     */
    public BigDecimal discountRate(Customer customer, String couponCode, BigDecimal subtotal) {
        BigDecimal rate = BigDecimal.ZERO;

        if (customer != null && customer.getTier() != null) {
            switch (customer.getTier()) {
                case "PLATINUM":
                    rate = rate.add(new BigDecimal("0.20"));
                    break;
                case "GOLD":
                    rate = rate.add(new BigDecimal("0.12"));
                    break;
                case "SILVER":
                    rate = rate.add(new BigDecimal("0.06"));
                    break;
                default:
                    rate = rate.add(BigDecimal.ZERO);
            }
        }

        if (couponCode != null) {
            if (couponCode.startsWith("VIP")) {
                if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
                    rate = rate.add(new BigDecimal("0.10"));
                } else {
                    rate = rate.add(new BigDecimal("0.05"));
                }
            } else if (couponCode.startsWith("SAVE")) {
                rate = rate.add(new BigDecimal("0.03"));
            }
        }

        if (subtotal.compareTo(new BigDecimal("1000")) >= 0) {
            rate = rate.add(new BigDecimal("0.05"));
        } else if (subtotal.compareTo(new BigDecimal("250")) >= 0) {
            rate = rate.add(new BigDecimal("0.02"));
        }

        // Cap total discount at 35%.
        BigDecimal cap = new BigDecimal("0.35");
        if (rate.compareTo(cap) > 0) {
            rate = cap;
        }
        return rate;
    }

    public BigDecimal total(Customer customer, String couponCode, List<OrderItem> items) {
        BigDecimal subtotal = subtotal(items);
        BigDecimal rate = discountRate(customer, couponCode, subtotal);
        BigDecimal discounted = subtotal.multiply(BigDecimal.ONE.subtract(rate));
        return discounted.setScale(2, RoundingMode.HALF_UP);
    }
}
