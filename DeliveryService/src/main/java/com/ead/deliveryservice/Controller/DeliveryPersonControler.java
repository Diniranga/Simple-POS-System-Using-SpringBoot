package com.ead.deliveryservice.Controller;

import com.ead.deliveryservice.Entity.DeliveryPerson;
import com.ead.deliveryservice.Entity.OrderRequest;
import com.ead.deliveryservice.Service.DeliveryPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveryPerson")
public class DeliveryPersonControler {

    @Autowired
    private DeliveryPersonService deliveryPersonService;

    @GetMapping("/getAllDeliveryPerson")
    public ResponseEntity<?> getAllDeliveryPerson() {
        return deliveryPersonService.getAllDeliveryPerson();
    }

    @GetMapping("/getDeliveryPersonById/{deliveryPersonId}")
    public DeliveryPerson getDeliveryPersonById(@PathVariable String deliveryPersonId){
        return deliveryPersonService.getDeliverPersonById(deliveryPersonId);
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveDeliveryPerson(@RequestBody DeliveryPerson deliveryPerson) {
        return deliveryPersonService.saveDeliveryPerson(deliveryPerson);
    }

    @PutMapping("/update/{deliveryPersonId}")
    public ResponseEntity<?> updateDeliveryPerson(@PathVariable String deliveryPersonId,@RequestBody DeliveryPerson updatedDeliveryPerson) {
        return deliveryPersonService.updateDeliveryPerson(deliveryPersonId,updatedDeliveryPerson);
    }

    @PostMapping("/assignOrderToDeliver")
    public ResponseEntity<?> assignOrder(@RequestBody OrderRequest orderRequest){
        return deliveryPersonService.assignOrder(orderRequest);
    }

    @PutMapping("/updateStatus/{deliveryPersonId}/{orderID}/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable String deliveryPersonId, @PathVariable String orderID, @PathVariable String status){
        return deliveryPersonService.updateStatus(deliveryPersonId, orderID, status);
    }


}
