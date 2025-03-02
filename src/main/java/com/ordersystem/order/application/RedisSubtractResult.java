package com.ordersystem.order.application;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisSubtractResult {

    private final ResultCode resultCode;
    private final Set<Long> soldOutIds;
    private final Set<Long> notFoundIds;

    public static RedisSubtractResult from(ResultCode resultCode, List<Long> soldOutIds, List<Long> notFoundIds) {
        return new RedisSubtractResult(resultCode, new HashSet<>(soldOutIds), new HashSet<>(notFoundIds));
    }

    public static RedisSubtractResult from(ResultCode resultCode) {
        return new RedisSubtractResult(resultCode, Set.of(), Set.of());
    }

    public boolean isSuccess() {
        return resultCode.isSuccess();
    }

    public boolean isSoldOut() {
        return !soldOutIds.isEmpty();
    }

    public boolean isNotFound() {
        return !notFoundIds.isEmpty();
    }
}
