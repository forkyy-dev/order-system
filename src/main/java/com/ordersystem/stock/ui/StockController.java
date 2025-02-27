package com.ordersystem.stock.ui;

import com.ordersystem.stock.application.StockService;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.ui.dto.StockCreateRequest;
import com.ordersystem.stock.ui.dto.StockModifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{stockId}")
    private ResponseEntity<StockDto> modify(
            @Valid @PathVariable Long stockId,
            @Valid @RequestBody StockModifyRequest request
    ) {
        StockDto result = stockService.modify(request.toStockModifyDto(stockId));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{stockId}")
    private ResponseEntity<Void> delete(@Valid @PathVariable Long stockId) {
        stockService.delete(stockId);
        return ResponseEntity.ok(null);
    }
}
