package com.ordersystem.stock.application;

import com.ordersystem.stock.application.dto.StockCacheDto;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockCacheService {

    private final StockRepository stockRepository;
    private final StockRedisManager stockRedisManager;

    public void updateStocksInCache(final Set<Long> stockIds) {
        List<Stock> stocks = stockRepository.findAllById(stockIds);
        List<StockCacheDto> cacheDtos = stocks.stream()
                .map(StockCacheDto::from)
                .toList();

        stockRedisManager.setStock(cacheDtos);
    }
}
