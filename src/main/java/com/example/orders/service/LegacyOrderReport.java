package com.example.orders.service;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderItem;
import com.example.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A deliberately smelly "God Class": it formats reports, aggregates totals,
 * classifies orders, and builds CSV all in one place, with no tests. This is
 * the kind of high-risk, high-complexity, untested code Prethink flags.
 */
public class LegacyOrderReport {

    public String build(List<Order> orders, String format) {
        StringBuilder sb = new StringBuilder();
        Map<OrderStatus, Integer> counts = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Order order : orders) {
            counts.merge(order.getStatus(), 1, Integer::sum);
            grandTotal = grandTotal.add(order.getTotal());

            String tag;
            if (order.getTotal().compareTo(new BigDecimal("1000")) >= 0) {
                tag = "LARGE";
            } else if (order.getTotal().compareTo(new BigDecimal("250")) >= 0) {
                tag = "MEDIUM";
            } else {
                tag = "SMALL";
            }

            if ("CSV".equalsIgnoreCase(format)) {
                sb.append(order.getId()).append(',')
                        .append(order.getStatus()).append(',')
                        .append(tag).append(',')
                        .append(itemCount(order)).append(',')
                        .append(order.getTotal()).append('\n');
            } else if ("TEXT".equalsIgnoreCase(format)) {
                sb.append("Order #").append(order.getId())
                        .append(" [").append(order.getStatus()).append(']')
                        .append(" ").append(tag)
                        .append(" items=").append(itemCount(order))
                        .append(" total=").append(order.getTotal())
                        .append('\n');
            } else {
                sb.append(order.getId()).append(' ').append(order.getTotal()).append('\n');
            }
        }

        if ("TEXT".equalsIgnoreCase(format)) {
            sb.append("--- Summary ---\n");
            for (Map.Entry<OrderStatus, Integer> e : counts.entrySet()) {
                sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
            }
            sb.append("Grand total: ").append(grandTotal).append('\n');
        }

        return sb.toString();
    }

    private int itemCount(Order order) {
        int total = 0;
        for (OrderItem item : order.getItems()) {
            total += item.getQuantity();
        }
        return total;
    }
}
