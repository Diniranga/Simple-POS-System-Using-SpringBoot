package com.ead.deliveryservice.Repository;


import com.ead.deliveryservice.Entity.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeliveryRepository extends MongoRepository<Delivery,String> {
    Delivery findFirstByOrderByDeliveryIdDesc();
    Delivery getDeliveryByDeliveryId(String deliveryId);
}
