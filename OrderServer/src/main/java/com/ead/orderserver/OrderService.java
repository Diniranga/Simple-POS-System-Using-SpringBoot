package com.ead.orderserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    public String generateProductId(){
        Order lastOrder = orderRepository.findFirstByOrderByOrderIdDesc();
        if(lastOrder != null){
            int lastOrderId = Integer.parseInt(lastOrder.getOrderId().substring(1));
            int newOrderId = lastOrderId + 1;
            return "O" + newOrderId;
        }else {
            return "O1";
        }
    }

    public LocalDateTime getCurrentTime(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.toLocalDateTime();
    }

    public ResponseEntity<Order> saveOrder(Order order){
        try {
            if(order.getCustomerId() == null){
                return ResponseEntity.badRequest().body(null);
            }
            order.setOrderId(generateProductId());
            order.setOrderTime(getCurrentTime());
            order.setOrderStatus(OrderStatus.IN_QUEUE);
            return ResponseEntity.ok(orderRepository.save(order));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    public ResponseEntity<?> getOrderByOrderId(String orderId){
        return ResponseEntity.ok(orderRepository.getOrderByOrderId(orderId));
    }

    public ResponseEntity<?> setStatus(String orderId, OrderStatus orderStatus){
        Order order = orderRepository.getOrderByOrderId(orderId);
        if(order == null){
            return ResponseEntity.badRequest().body("Order not found");
        }
        RestTemplate restTemplate = new RestTemplate();
        String orderUrl = "http://localhost:8080/customers/setOrderStatus/" + order.getCustomerId() + "/" + orderStatus;
        ResponseEntity<Order> savedOrder = restTemplate.postForEntity(orderUrl,order,Order.class);

        order.setOrderStatus(orderStatus);
        return ResponseEntity.ok(orderRepository.save(order));
    }



//    WRITE A  CODE TO DELETE ORDER
}
