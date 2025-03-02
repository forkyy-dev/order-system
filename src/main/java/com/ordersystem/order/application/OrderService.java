package com.ordersystem.order.application;

import com.ordersystem.common.dto.PageDto;
import com.ordersystem.common.exception.ApplicationException;
import com.ordersystem.order.application.dto.*;
import com.ordersystem.order.domain.*;
import com.ordersystem.order.exception.AlreadyCanceledException;
import com.ordersystem.order.exception.OrderNotFoundException;
import com.ordersystem.order.infra.StockCustomRepository;
import com.ordersystem.stock.application.StockRedisManager;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.events.StockEventProducer;
import com.ordersystem.stock.exception.StockNotFoundException;
import com.ordersystem.stock.exception.StockSoldOutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final StockRedisManager stockQuantityManager;
    private final StockEventProducer stockEventProducer;

    @Transactional
    public OrderDto createOrder(CreateOrderDto dto) {
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

        // 1-4. Redis에 해당 상품들에 대한 데이터가 없다고 이벤트 발행 및 DB 직접 접근해서 재고 검증
        if (subtractResult.isNotFound()) {
            stockEventProducer.produceStockCacheUpdateEvent(subtractResult.getNotFoundIds());
            fallbackToDatabase(dto.getIdQuantityPair());
            return processOrder(dto, stocks, stockIdQuantityPair);
        }

        throw new ApplicationException("알 수 없는 예외 발생");
    }

    @Transactional
    public OrderDto confirmOrder(ConfirmOrderDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(dto.getOrderId()));
        List<OrderStock> orderStocks = orderStockRepository.findAllByOrderId(order.getId());

        //결제 성공 시 DB상의 재고를 업데이트한 뒤 주문의 상태를 변경한다.
        if (dto.isPaymentSuccess()) {
            Map<Long, Integer> stockIdQuantityPair = orderStocks.stream().collect(Collectors.toMap(OrderStock::getStockId, OrderStock::getQuantity));
            updateStockOriginQuantity(stockIdQuantityPair, StockUpdateCondition.SUBTRACT);
            order.confirm();
            insertOrderStatusHistory(order);
        }
        return OrderDto.from(order, orderStocks);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        List<OrderStock> orderStocks = orderStockRepository.findAllByOrderId(order.getId());
        if (order.isCanceled()) {
            throw new AlreadyCanceledException(order.getId());
        }

        //이미 확정된 주문의 경우 DB 상의 상품 재고를 바로 변경한다.
        if (order.isConfirmed()) {
            Map<Long, Integer> stockIdQuantityPair = orderStocks.stream().collect(Collectors.toMap(OrderStock::getStockId, OrderStock::getQuantity));
            updateStockOriginQuantity(stockIdQuantityPair, StockUpdateCondition.INCREASE);
        }
        order.cancel();
        insertOrderStatusHistory(order);
        return OrderDto.from(order, orderStocks);
    }

    @Transactional(readOnly = true)
    public OrderInfoPaginationDto getAllOrderInfoByPagination(Long userId, Pageable pageable) {
        Slice<Order> orderSlice = orderRepository.findAllByUserIdOrderByOrderDateDesc(userId, pageable);

        PageDto pageDto = new PageDto(orderSlice.getNumberOfElements(), orderSlice.getPageable().getPageNumber(), orderSlice.isLast());
        if (orderSlice.getContent().isEmpty()) {
            return OrderInfoPaginationDto.empty(pageDto);
        }

        Map<Long, List<OrderStock>> orderStockPair = fetchOrderStocksAndStocks(orderSlice.getContent().stream().map(Order::getId).toList());
        Map<Long, Stock> stockPair = fetchStockPair(orderStockPair);

        List<OrderInfoDto> orderInfoDtos = orderSlice.getContent().stream()
                .map(order -> convertToOrderInfoDto(order, orderStockPair, stockPair))
                .toList();

        return OrderInfoPaginationDto.from(orderInfoDtos, pageDto);
    }

    @Transactional(readOnly = true)
    public OrderInfoDto getSingleOrderInfo(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Map<Long, List<OrderStock>> orderStockPair = fetchOrderStocksAndStocks(List.of(orderId));
        Map<Long, Stock> stockPair = fetchStockPair(orderStockPair);

        return convertToOrderInfoDto(order, orderStockPair, stockPair);
    }

    private Map<Long, List<OrderStock>> fetchOrderStocksAndStocks(List<Long> orderIds) {
        List<OrderStock> orderStocks = orderStockRepository.findAllByOrderIdIsIn(orderIds);
        return orderStocks.stream().collect(Collectors.groupingBy(OrderStock::getOrderId));
    }

    private Map<Long, Stock> fetchStockPair(Map<Long, List<OrderStock>> orderStockPair) {
        List<Long> stockIds = orderStockPair.values().stream()
                .flatMap(List::stream)
                .map(OrderStock::getStockId)
                .distinct()
                .toList();

        return stockRepository.findAllByIdIsIn(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, stock -> stock));
    }

    private OrderInfoDto convertToOrderInfoDto(Order order, Map<Long, List<OrderStock>> orderStockPair, Map<Long, Stock> stockPair) {
        List<OrderStock> orderStockList = orderStockPair.getOrDefault(order.getId(), List.of());
        List<OrderStockInfoDto> orderStockInfos = orderStockList.stream()
                .map(orderStock -> OrderStockInfoDto.from(orderStock, stockPair.get(orderStock.getStockId())))
                .toList();

        return OrderInfoDto.from(order, orderStockInfos);
    }

    private void fallbackToDatabase(Map<Long, Integer> stockIdQuantityPair) {
        List<Stock> stocks = stockRepository.findAllById(stockIdQuantityPair.keySet());
        if (stocks.isEmpty()) {
            throw new StockNotFoundException();
        }
        if (stocks.stream().anyMatch(s -> !s.isPurchasable(stockIdQuantityPair.get(s.getId())))) {
            throw new StockSoldOutException();
        }
    }

    private OrderDto processOrder(CreateOrderDto dto, List<Stock> stocks, Map<Long, Integer> stockIdQuantityPair) {
        double orderTotalPrice = calculateOrderTotalPrice(stocks, stockIdQuantityPair);
        Order newOrder = orderRepository.save(new Order(orderTotalPrice, dto.getUserId()));
        List<OrderStock> newOrderStocks = orderStockRepository.saveAll(createOrderStocks(stocks, stockIdQuantityPair, newOrder));
        insertOrderStatusHistory(newOrder);
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

    private void insertOrderStatusHistory(Order order) {
        orderStatusHistoryRepository.save(new OrderStatusHistory(order.getId(), order.getStatus(), StatusChangeReason.CONFIRM_ORDER));
    }

    private void updateStockOriginQuantity(Map<Long, Integer> stockIdQuantityPair, StockUpdateCondition condition) {
        int resultRowCount = stockCustomRepository.updateStockQuantities(stockIdQuantityPair, condition);
        if (resultRowCount != stockIdQuantityPair.size()) {
            throw new StockSoldOutException();
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
