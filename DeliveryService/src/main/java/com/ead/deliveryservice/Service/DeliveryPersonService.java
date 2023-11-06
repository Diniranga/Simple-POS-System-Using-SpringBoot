package com.ead.deliveryservice.Service;

import com.ead.deliveryservice.Entity.DeliveryPerson;
import com.ead.deliveryservice.Repository.DeliveryPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DeliveryPersonService {

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    public String generateDeliveryPersonId() {
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findFirstByOrderByDeliveryPersonIdDesc();
        if (deliveryPerson != null) {
            String deliveryPersonId = deliveryPerson.getDeliveryPersonId();
            int id = Integer.parseInt(deliveryPersonId.substring(2)) + 1;
            return "DP" + id;
        } else {
            return "DP1";
        }
    }

    public ResponseEntity<?> getAllDeliveryPerson() {
        return deliveryPersonRepository.findAll().isEmpty() ? ResponseEntity.ok("No Delivery Person Found") : ResponseEntity.ok(deliveryPersonRepository.findAll());
    }

    public DeliveryPerson getDeliverPersonById(String deliverPersonId){
        return deliveryPersonRepository.getDeliveryPersonByDeliveryPersonId(deliverPersonId);
    }

    public ResponseEntity<?> saveDeliveryPerson(DeliveryPerson deliveryPerson) {
        if(deliveryPersonRepository.getDeliveryPersonByEmail(deliveryPerson.getEmail()) != null){
            return ResponseEntity.badRequest().body("Email already exists");
        }
        deliveryPerson.setDeliveryPersonId(generateDeliveryPersonId());
        return ResponseEntity.ok(deliveryPersonRepository.save(deliveryPerson));
    }

    public ResponseEntity<?> updateDeliveryPerson(String deliverPersonId,DeliveryPerson deliveryPerson){
        DeliveryPerson updatedDeliverPerson = deliveryPersonRepository.getDeliveryPersonByDeliveryPersonId(deliverPersonId);
        if(updatedDeliverPerson == null){
            return ResponseEntity.badRequest().body("No Delivery Person Found");
        }
        updatedDeliverPerson.setFirstName(deliveryPerson.getFirstName());
        updatedDeliverPerson.setLastName(deliveryPerson.getLastName());
        updatedDeliverPerson.setContactNumber(deliveryPerson.getContactNumber());
        updatedDeliverPerson.setAddress(deliveryPerson.getAddress());
        return ResponseEntity.ok(deliveryPersonRepository.save(updatedDeliverPerson));
    }
}
