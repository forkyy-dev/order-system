package com.ordersystem.stock.ui;

import com.ordersystem.stock.application.StockService;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.application.dto.StockPaginationDto;
import com.ordersystem.stock.application.dto.SearchStockDto;
import com.ordersystem.stock.ui.dto.StockCreateRequest;
import com.ordersystem.stock.ui.dto.StockModifyRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/stock")
    private ResponseEntity<StockDto> create(@Valid @RequestBody StockCreateRequest request) {
        StockDto result = stockService.create(request.toStockCreateDto());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/stock/{stockId}")
    private ResponseEntity<StockDto> modify(
            @Valid @PathVariable Long stockId,
            @Valid @RequestBody StockModifyRequest request
    ) {
        StockDto result = stockService.modify(request.toStockModifyDto(stockId));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/stock/{stockId}")
    private ResponseEntity<Void> delete(@Valid @PathVariable Long stockId) {
        stockService.delete(stockId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/stocks")
    private ResponseEntity<StockPaginationDto> search(
            @NotNull @RequestParam Long categoryId,
            @RequestParam String stockName,
            @PageableDefault Pageable pageable
            ) {
        StockPaginationDto result = stockService.search(new SearchStockDto(categoryId, stockName, pageable));
        return ResponseEntity.ok(result);
    }
}
