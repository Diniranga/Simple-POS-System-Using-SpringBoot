package com.ead.orderserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String orderId;
    private String customerId;
    private LocalDateTime orderTime;
    private List<OrderItem> orderItems;
    private OrderStatus orderStatus;
    private String deliveryPersonId;

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderTime=" + orderTime +
                ", orderItems=" + orderItems +
                ", orderStatus=" + orderStatus +
                ", deliveryPersonId='" + deliveryPersonId + '\'' +
                '}';
    }

}
