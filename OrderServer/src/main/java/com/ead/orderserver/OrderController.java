package com.ead.orderserver;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<Order> saveOrder(@RequestBody Order order){
        return orderService.saveOrder(order);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllOrders(){
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/getById/{orderId}")
    public ResponseEntity<?> getOrderByOrderId(@PathVariable String orderId){
        return orderService.getOrderByOrderId(orderId);
    }

    @PutMapping("/setStatus/{orderId}/{orderStatus}")
    public ResponseEntity<?> setStatus(@PathVariable String orderId, @PathVariable OrderStatus orderStatus) {
        return orderService.setStatus(orderId, orderStatus);
    }

    @GetMapping("/getOrderByStatus/{orderStatus}")
    public ResponseEntity<?> getOrderByOrderStatus(@PathVariable OrderStatus orderStatus){
        return ResponseEntity.ok(orderService.getOrderByOrderStatus(orderStatus));
    }

    @PutMapping("/cancelOrder/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId){
        return orderService.cancelOrder(orderId);
    }

}


