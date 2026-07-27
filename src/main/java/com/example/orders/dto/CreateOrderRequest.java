package com.example.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderRequest {

    @NotNull
    private Long customerId;

    @NotEmpty
    @Valid
    private List<OrderLineRequest> lines;

    private String couponCode;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderLineRequest> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineRequest> lines) {
        this.lines = lines;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
