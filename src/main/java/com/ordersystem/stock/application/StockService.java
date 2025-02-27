package com.ordersystem.stock.application;

import com.ordersystem.stock.application.dto.StockCreateDto;
import com.ordersystem.stock.application.dto.StockDto;
import com.ordersystem.stock.application.dto.StockModifyDto;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.exception.DuplicatedStockException;
import com.ordersystem.stock.exception.StockNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public StockDto create(final StockCreateDto dto) {
        if (stockRepository.existsByNameAndCategoryId(dto.getName(), dto.getCategoryId())) {
            throw new DuplicatedStockException(dto.getName());
        }
        Stock stock = stockRepository.save(dto.toEntity());
        return StockDto.from(stock);
    }

    @Transactional
    public StockDto modify(final StockModifyDto dto) {
        Stock stock = stockRepository.findById(dto.getId()).orElseThrow(() -> new StockNotFoundException(dto.getId()));
        stock.updateInfo(dto.getName(), dto.getPrice(), dto.getCurrentQuantity(), dto.getMaxQuantity(), dto.getCategoryId());
        return StockDto.from(stock);
    }

    public void delete(Long stockId) {
        Stock stock = stockRepository.findById(stockId).orElseThrow(() -> new StockNotFoundException(stockId));

        stockRepository.delete(stock);
    }
}

