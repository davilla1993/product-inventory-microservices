package com.gbossoufolly.com.inventoryservice.controllers;

import com.gbossoufolly.com.inventoryservice.dto.InventoryResponse;
import com.gbossoufolly.com.inventoryservice.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory")
public class InventoryController {


    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    //  http://localhost:8082/api/inventory?skuCode=iphone-15&skuCode=macbook-pro16
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {

        return inventoryService.isInStock(skuCode);
    }
}
