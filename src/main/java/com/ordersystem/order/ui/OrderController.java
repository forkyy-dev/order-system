package com.ordersystem.order.ui;

import com.ordersystem.order.application.OrderService;
import com.ordersystem.order.application.dto.OrderDto;
import com.ordersystem.order.ui.dto.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    private ResponseEntity<OrderDto> createNewOrder(@RequestBody OrderCreateRequest request) {
        OrderDto result = orderService.createOrder(request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
