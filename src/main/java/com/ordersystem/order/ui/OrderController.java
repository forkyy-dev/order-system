package com.ordersystem.order.ui;

import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.ConfirmOrderDto;
import com.ordersystem.order.application.dto.OrderDto;
import com.ordersystem.order.application.dto.OrderInfoDto;
import com.ordersystem.order.application.dto.OrderInfoPaginationDto;
import com.ordersystem.order.ui.dto.ConfirmOrderRequest;
import com.ordersystem.order.ui.dto.CreateOrderRequest;
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
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    private ResponseEntity<OrderDto> createNewOrder(@RequestBody CreateOrderRequest request) {
        OrderDto result = orderService.createOrder(request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/order/{orderId}/confirm")
    private ResponseEntity<OrderDto> confirmOrder(
            @NotNull @PathVariable Long orderId,
            @RequestBody ConfirmOrderRequest request
    ) {
        OrderDto result = orderService.confirmOrder(ConfirmOrderDto.from(orderId, request.getPaymentSuccess()));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/order/{orderId}/cancel")
    private ResponseEntity<OrderDto> createNewOrder(@NotNull @PathVariable Long orderId) {
        OrderDto result = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(result);
    }

    //INFO: user 구현은 이번 과제의 범위를 벗어나는것 같아서 임시로 RequestParam을 통해 전달받도록 구현하였습니다.
    @GetMapping("/orders")
    private ResponseEntity<OrderInfoPaginationDto> searchMultiOrders(
            @NotNull @RequestParam Long userId,
            @PageableDefault Pageable pageable
    ) {
        OrderInfoPaginationDto result = orderService.getAllOrderInfoByPagination(userId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders/{orderId}")
    private ResponseEntity<OrderInfoDto> searchMultiOrders(
            @NotNull @RequestParam Long userId,
            @NotNull @PathVariable Long orderId
    ) {
        OrderInfoDto result = orderService.getSingleOrderInfo(userId, orderId);
        return ResponseEntity.ok(result);
    }
}
