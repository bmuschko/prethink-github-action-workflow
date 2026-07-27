package com.example.orders.controller;

import com.example.orders.domain.Order;
import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/{id}/place")
    public Order place(@PathVariable Long id) {
        return orderService.placeOrder(id);
    }

    @PostMapping("/{id}/ship")
    public Order ship(@PathVariable Long id) {
        return orderService.ship(id);
    }

    @GetMapping
    public List<Order> byCustomer(@RequestParam Long customerId) {
        return orderService.ordersForCustomer(customerId);
    }
}
