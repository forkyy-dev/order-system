package com.ordersystem.stock.ui;

import com.ordersystem.stock.application.StockService;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.ui.dto.StockCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/stock")
@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    private ResponseEntity<StockDto> create(@Valid @RequestBody StockCreateRequest request) {
        StockDto result = stockService.create(request.toStockCreateDto());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
