package com.ead.deliveryservice.Service;

import com.ead.deliveryservice.Entity.Delivery;
import com.ead.deliveryservice.Enum.DeliveryStatus;
import com.ead.deliveryservice.Repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public String generateDeliveryId(){
        Delivery lastDelivery = deliveryRepository.findFirstByOrderByDeliveryIdDesc();
        if(lastDelivery != null){
            int lastDeliveryId = Integer.parseInt(lastDelivery.getDeliveryId().substring(1));
            int newDeliveryId = lastDeliveryId + 1;
            return "D" + newDeliveryId;
        }else {
            return "D1";
        }
    }

    public ResponseEntity<?> addDelivery(Delivery delivery) {
        delivery.setDeliveryId(generateDeliveryId());
        return ResponseEntity.ok(deliveryRepository.save(delivery));
    }

    public ResponseEntity<String> updateStatus(String deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.getDeliveryByDeliveryId(deliveryId);
        if (delivery == null) {
            return ResponseEntity.badRequest().body("Delivery not found");
        }
        delivery.setDeliveryStatus(status);
        deliveryRepository.save(delivery);
        return ResponseEntity.ok("Delivery status updated successfully");
    }
}
