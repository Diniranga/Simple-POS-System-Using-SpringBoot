package com.ead.orderserver;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Order findFirstByOrderByOrderIdDesc();
    Order getOrderByOrderId(String orderId);
}
