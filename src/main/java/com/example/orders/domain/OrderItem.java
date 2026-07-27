package com.example.orders.domain;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class OrderItem {

    private String sku;
    private int quantity;
    private BigDecimal unitPrice;

    protected OrderItem() {
    }

    public OrderItem(String sku, int quantity, BigDecimal unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
