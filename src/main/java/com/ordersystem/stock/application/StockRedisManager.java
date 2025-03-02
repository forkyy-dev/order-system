package com.ordersystem.stock.application;

import com.ordersystem.order.application.RedisConcurrencyManager;
import com.ordersystem.order.application.RedisSubtractResult;
import com.ordersystem.order.application.ResultCode;
import com.ordersystem.order.application.dto.CreateOrderDto;
import com.ordersystem.stock.application.dto.StockCacheDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockRedisManager{

    private static final String STOCK_KEY_FORMAT = "stock:";
    private final RedisConcurrencyManager concurrencyManager;

    @Transactional
    public RedisSubtractResult subtractStock(CreateOrderDto dto) {
        return concurrencyManager.subtractMultipleStocks(dto.getKeyQuantityPair(STOCK_KEY_FORMAT));
    }

    @Transactional
    public ResultCode setStock(List<StockCacheDto> cacheDtos) {
        log.debug("없는 상품 정보 캐시 등록 시작");
        Map<String, Integer> keyQuantityPair = cacheDtos.stream().collect(Collectors.toMap(dto -> STOCK_KEY_FORMAT + dto.getId(), StockCacheDto::getQuantity));
        return concurrencyManager.setMultipleStocks(keyQuantityPair);
    }

}
