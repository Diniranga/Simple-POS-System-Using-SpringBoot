package com.ead.inventorymanagerservice.Repository;

import com.ead.inventorymanagerservice.Entity.InventoryManager;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryManagerRepository extends MongoRepository<InventoryManager,String> {
    Optional<InventoryManager> findInventoryManagerByInventoryManagerId(String inventoryManagerId);
    InventoryManager findFirstByOrderByInventoryManagerIdDesc();
    InventoryManager findInventoryManagerByEmail(String email);

}