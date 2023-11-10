package com.ead.inventorymanagerservice.Controller;

import com.ead.inventorymanagerservice.Entity.InventoryManager;
import com.ead.inventorymanagerservice.Service.InventoryManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventoryManager")
public class InventoryManagerController {

    @Autowired
    private InventoryManagerService inventoryManagerService;

    @GetMapping("/getAll")
    public List<InventoryManager> getAll(){
        return inventoryManagerService.getAll();
    }

    @GetMapping("/getById/{inventoryManagerId}")
    public ResponseEntity<?> getById(@PathVariable String inventoryManagerId){
        return inventoryManagerService.getById(inventoryManagerId);
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveManager(@RequestBody InventoryManager inventoryManager){
        return inventoryManagerService.saveInventoryManager(inventoryManager);
    }

    @PostMapping("/update/{inventoryManagerId}")
    public ResponseEntity<?> updateinventoryManagerId(@PathVariable String inventoryManagerId,@RequestBody InventoryManager inventoryManager){
        return inventoryManagerService.updateInventoryManagerDetails(inventoryManagerId,inventoryManager);
    }

    @DeleteMapping("/deleteById/{inventoryManagerId}")
    public ResponseEntity<?> deleteManager(@PathVariable String inventoryManagerId){
        return inventoryManagerService.deleteManagerById(inventoryManagerId);
    }
}
