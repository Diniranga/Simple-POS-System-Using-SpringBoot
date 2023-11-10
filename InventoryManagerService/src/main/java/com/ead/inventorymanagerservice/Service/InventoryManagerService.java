package com.ead.inventorymanagerservice.Service;

import com.ead.inventorymanagerservice.Exceptions.UserAlreadyExistsException;
import com.ead.inventorymanagerservice.Exceptions.UserNotFoundException;
import com.ead.inventorymanagerservice.Entity.InventoryManager;
import com.ead.inventorymanagerservice.Repository.InventoryManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class InventoryManagerService {

    @Autowired
    private InventoryManagerRepository inventoryManagerRepository;


    public String generateInventoryManagerId() {
        InventoryManager lastCustomer = inventoryManagerRepository.findFirstByOrderByInventoryManagerIdDesc();
        if (lastCustomer != null) {
            int lastUserId = Integer.parseInt(lastCustomer.getInventoryManagerId().substring(1));
            int newUserId = lastUserId + 1;
            return "IM" + newUserId;
        } else {
            return "IM1";
        }
    }
    public List<InventoryManager> getAll() {
        return inventoryManagerRepository.findAll();
    }

    public ResponseEntity<?> getById(String inventoryManagerId) {
        return ResponseEntity.ok(inventoryManagerRepository.findInventoryManagerByInventoryManagerId(inventoryManagerId));
    }

    public ResponseEntity<?> saveInventoryManager(InventoryManager inventoryManager) {
        try {
            InventoryManager existingInventoryManager = inventoryManagerRepository.findInventoryManagerByEmail(inventoryManager.getEmail());
            if (existingInventoryManager != null) {
                throw new UserAlreadyExistsException("Email: " + inventoryManager.getEmail() + " is already exist");
            }
            inventoryManager.setInventoryManagerId(generateInventoryManagerId());
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<?> response = restTemplate.getForEntity(
                    "http://localhost:8081/auth/checkEmailExists/{email}",
                    String.class,
                    inventoryManager.getEmail()
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String requestJson = "{" +
                        "\"userId\":\"" + inventoryManager.getInventoryManagerId() + "\"," +
                        "\"firstName\":\"" + inventoryManager.getFirstName() + "\"," +
                        "\"lastName\":\"" + inventoryManager.getLastName() + "\"," +
                        "\"email\":\"" + inventoryManager.getEmail() + "\"," +
                        "\"password\":\"" + inventoryManager.getPassword() + "\"," +
                        "\"role\":\"INVENTORY_KEEPER\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
                ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                        "http://localhost:8081/auth/register",
                        requestEntity,
                        String.class
                );

                if (registerResponse.getStatusCode() == HttpStatus.OK) {
                    inventoryManagerRepository.save(inventoryManager);
                    return ResponseEntity.ok("InventoryManager saved successfully");
                } else {
                    throw new UserAlreadyExistsException("Error registering Inventory Manager");
                }
            } else {
                throw new UserAlreadyExistsException("Email: " + inventoryManager.getEmail() + " is already exist");
            }
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().body("Email: " + inventoryManager.getEmail() + " is already exist");
        }
    }

    public ResponseEntity<?> updateInventoryManagerDetails(String inventoryManagerId, InventoryManager inventoryManager) {
        InventoryManager currentManager = inventoryManagerRepository.findInventoryManagerByInventoryManagerId(inventoryManagerId).orElseThrow(() -> new UserNotFoundException("Inventory Manager Not Found"));
        currentManager.setFirstName(inventoryManager.getFirstName());
        currentManager.setLastName(inventoryManager.getLastName());
        currentManager.setAddress(inventoryManager.getAddress());
        currentManager.setEmail(inventoryManager.getEmail());
        currentManager.setContactNumber(inventoryManager.getContactNumber());
        return ResponseEntity.ok(inventoryManagerRepository.save(currentManager));
    }

    public ResponseEntity<?> deleteManagerById(String inventoryManagerId) {
        try {
            InventoryManager currentManager = inventoryManagerRepository.findInventoryManagerByInventoryManagerId(inventoryManagerId)
                    .orElseThrow(() -> new UserNotFoundException("Inventory Manager Not Found to delete"));
            inventoryManagerRepository.delete(currentManager);
            return ResponseEntity.ok("InventoryManager deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
