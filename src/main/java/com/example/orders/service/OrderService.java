package com.example.orders.service;

import com.example.orders.domain.Customer;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderItem;
import com.example.orders.domain.OrderStatus;
import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderLineRequest;
import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.repository.CustomerRepository;
import com.example.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final PricingService pricingService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        PricingService pricingService,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown customer: " + request.getCustomerId()));

        Order order = new Order(customer.getId());
        for (OrderLineRequest line : request.getLines()) {
            order.addItem(new OrderItem(line.getSku(), line.getQuantity(), line.getUnitPrice()));
        }
        order.setTotal(pricingService.total(customer, request.getCouponCode(), order.getItems()));
        return orderRepository.save(order);
    }

    @Transactional
    public Order placeOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be placed");
        }
        order.setStatus(OrderStatus.PLACED);
        Order saved = orderRepository.save(order);
        customerRepository.findById(order.getCustomerId())
                .ifPresent(customer -> notificationService.orderPlaced(customer, saved));
        return saved;
    }

    @Transactional
    public Order ship(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only paid orders can ship");
        }
        order.setStatus(OrderStatus.SHIPPED);
        notificationService.orderShipped(order);
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<Order> ordersForCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}
