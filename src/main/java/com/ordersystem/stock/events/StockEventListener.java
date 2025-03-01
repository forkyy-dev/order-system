package com.ordersystem.stock.events;

import com.ordersystem.stock.application.StockCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final StockCacheService stockCacheService;

    @Async
    @EventListener
    public void updateStocksInCache(StockCacheUpdateEvent event) {
        stockCacheService.updateStocksInCache(event.getStockIds());
    }
}
