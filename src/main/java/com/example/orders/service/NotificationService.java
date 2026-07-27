package com.example.orders.service;

import com.example.orders.domain.Customer;
import com.example.orders.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stand-in for an email/messaging integration. In a real system this would
 * publish to Kafka or call an email provider.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void orderPlaced(Customer customer, Order order) {
        // Feature envy: this method reaches almost entirely into Customer/Order state.
        String recipient = customer.getEmail() == null ? "unknown" : customer.getEmail();
        log.info("Order {} placed for {} ({} {}), total {}",
                order.getId(), customer.getName(), recipient, customer.getTier(), order.getTotal());
    }

    public void orderShipped(Order order) {
        log.info("Order {} shipped", order.getId());
    }
}
