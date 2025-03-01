package com.ordersystem.order.application;

import com.ordersystem.common.exception.ApplicationException;
import com.ordersystem.order.application.dto.OrderCreateDto;
import com.ordersystem.order.application.dto.OrderDto;
import com.ordersystem.order.domain.Order;
import com.ordersystem.order.domain.OrderRepository;
import com.ordersystem.order.domain.OrderStock;
import com.ordersystem.order.domain.OrderStockRepository;
import com.ordersystem.order.infra.StockCustomRepository;
import com.ordersystem.stock.application.StockRedisManager;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.events.StockEventProducer;
import com.ordersystem.stock.exception.StockNotFoundException;
import com.ordersystem.stock.exception.StockSoldOutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final StockRepository stockRepository;
    private final StockCustomRepository stockCustomRepository;
    private final OrderRepository orderRepository;
    private final OrderStockRepository orderStockRepository;
    private final StockRedisManager stockQuantityManager;
    private final StockEventProducer stockEventProducer;

    @Transactional
    public OrderDto createOrder(OrderCreateDto dto) {
        Set<Long> orderStockIds = dto.getOrderStockIds();
        List<Stock> stocks = stockRepository.findAllById(orderStockIds);

        // 0.상품 존재 여부 검사
        validateStockAvailable(orderStockIds, stocks);

        // 1. Redis에서 상품 제거.
        RedisSubtractResult subtractResult;
        Map<Long, Integer> stockIdQuantityPair = dto.getIdQuantityPair();

        // 1-1. Redis에 접근에 실패할 경우 DB로 바로 접근.
        try {
            subtractResult = stockQuantityManager.subtractStock(dto);
        } catch (Exception e) {
            log.error("Redis 통신 실패: {}", e.getMessage());
            fallbackToDatabase(stockIdQuantityPair);
            return processOrder(dto, stocks, stockIdQuantityPair);
        }

        // 1-2. 성공 시 주문 생성 및 저장
        if (subtractResult.isSuccess()) {
            return processOrder(dto, stocks, stockIdQuantityPair);
        }

        // 1-3. 품절 된 상품이 존재할 경우 예외 발생
        if (subtractResult.isSoldOut()) {
            throw new StockSoldOutException(subtractResult.getSoldOutIds());
        }

        // 1-4. Redis에 해당 상품들에 대한 데이터가 없다고 이벤트 발행 및 DB 직접 접근
        if (subtractResult.isNotFound()) {
            stockEventProducer.produceStockCacheUpdateEvent(subtractResult.getNotFoundIds());
            fallbackToDatabase(stockIdQuantityPair);
            return processOrder(dto, stocks, stockIdQuantityPair);
        }

        throw new ApplicationException("알 수 없는 예외 발생");
    }

    private void fallbackToDatabase(Map<Long, Integer> stockIdQuantityPair) {
        int resultCount = stockCustomRepository.updateStockQuantities(stockIdQuantityPair);
        if (!isStockUpdateValid(stockIdQuantityPair, resultCount)) {
            throw new StockSoldOutException();
        }
    }

    private boolean isStockUpdateValid(Map<Long, Integer> stockIdQuantityPair, int resultCount) {
        return resultCount == stockIdQuantityPair.size();
    }

    private OrderDto processOrder(OrderCreateDto dto, List<Stock> stocks, Map<Long, Integer> stockIdQuantityPair) {
        double orderTotalPrice = calculateOrderTotalPrice(stocks, stockIdQuantityPair);
        Order newOrder = orderRepository.save(new Order(orderTotalPrice, dto.getUserId()));
        List<OrderStock> newOrderStocks = orderStockRepository.saveAll(createOrderStocks(stocks, stockIdQuantityPair, newOrder));
        return OrderDto.from(newOrder, newOrderStocks);
    }

    private List<OrderStock> createOrderStocks(List<Stock> stocks, Map<Long, Integer> stockIdQuantityPair, Order newOrder) {
        return stocks.stream()
                .map(s -> new OrderStock(stockIdQuantityPair.get(s.getId()), s.getPrice(), s.getId(), newOrder.getId()))
                .toList();
    }

    private double calculateOrderTotalPrice(List<Stock> stocks, Map<Long, Integer> stockIdQuantityPair) {
        return stocks.stream()
                .mapToDouble(s -> stockIdQuantityPair.get(s.getId()) * s.getPrice())
                .sum();
    }

    private void validateStockAvailable(Set<Long> orderStockIds, List<Stock> originalStocks) {
        Set<Long> stockIds = originalStocks
                .stream()
                .map(Stock::getId)
                .collect(Collectors.toSet());

        if (!stockIds.equals(orderStockIds)) {
            throw new StockNotFoundException();
        }
    }

    /*
    1. 유저가 주문하기를 누르면 최초 주문이 생성된다.
    2. 해당 주문 정보를 바탕으로 Order DB에 주문을 저장한다. - OrderHistory에도 Insert 한다.
    3. 이후 유저가 부가정보를 입력 후 결제 요청을 하게 되면
        - 결제 성공 시 해당 주문건을 주문 완료 상태로 변경한다. - OrderHistory에 Insert 한다.
        - 결제 실패 시 예외 반환.
    4. 만약 주문 취소를 하게 되면 주문건을 주문 취소 상태로 변경한다. - OrderHistory에 Insert 한다.
     */

}
