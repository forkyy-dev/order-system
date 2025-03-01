package com.ordersystem.stock.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockEventProducer {

    private final ApplicationEventPublisher publisher;

    public void produceStockCacheUpdateEvent(Set<Long> stockIds) {
        publisher.publishEvent(StockCacheUpdateEvent.newEvent(stockIds));
    }
}
