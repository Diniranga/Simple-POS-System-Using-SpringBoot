package com.ead.deliveryservice.Controller;

import com.ead.deliveryservice.Entity.Delivery;
import com.ead.deliveryservice.Enum.DeliveryStatus;
import com.ead.deliveryservice.Service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/getAll")
    public List<Delivery> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addDelivery(@RequestBody Delivery delivery) {
        return deliveryService.addDelivery(delivery);
    }

    @PutMapping("/updateStatus/{deliveryId}/{status}")
    public ResponseEntity<String> updateStatus(@PathVariable String deliveryId, @PathVariable DeliveryStatus status) {
        return deliveryService.updateStatus(deliveryId, status);
    }

}
