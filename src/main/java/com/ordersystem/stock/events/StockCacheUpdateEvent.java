package com.ordersystem.stock.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StockCacheUpdateEvent {
    private final Set<Long> stockIds;


    public static StockCacheUpdateEvent newEvent(Set<Long> stockIds) {
        return new StockCacheUpdateEvent(stockIds);
    }
}
