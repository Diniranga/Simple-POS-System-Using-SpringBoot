package com.ead.orderserver;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Order findFirstByOrderByOrderIdDesc();
    Order getOrderByOrderId(String orderId);
    List<Order> getOrderByOrderStatus(OrderStatus orderStatus);
}
