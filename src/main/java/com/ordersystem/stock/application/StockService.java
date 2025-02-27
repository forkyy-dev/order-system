package com.ordersystem.stock.application;

import com.ordersystem.stock.application.dto.StockCreateDto;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.DuplicatedStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public StockDto create(StockCreateDto dto) {
        if (stockRepository.existsByName(dto.getName())) {
            throw new DuplicatedStockException(dto.getName());
        }
        Stock stock = stockRepository.save(dto.toEntity());
        return StockDto.from(stock);
    }
}

