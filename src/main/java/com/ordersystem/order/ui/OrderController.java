package com.ordersystem.order.ui;

import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.ConfirmOrderDto;
import com.ordersystem.order.application.dto.OrderDto;
import com.ordersystem.order.ui.dto.ConfirmOrderRequest;
import com.ordersystem.order.ui.dto.CreateOrderRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/api/order")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    private ResponseEntity<OrderDto> createNewOrder(@RequestBody CreateOrderRequest request) {
        OrderDto result = orderService.createOrder(request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{orderId}/confirm")
    private ResponseEntity<OrderDto> confirmOrder(
            @NotNull @PathVariable Long orderId,
            @RequestBody ConfirmOrderRequest request) {
        OrderDto result = orderService.confirmOrder(ConfirmOrderDto.from(orderId, request.getPaymentSuccess()));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{orderId}/cancel")
    private ResponseEntity<OrderDto> createNewOrder(@NotNull @PathVariable Long orderId) {
        OrderDto result = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(result);
    }
}
