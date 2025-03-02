package com.ordersystem.order.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisConcurrencyManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public ResultCode setMultipleStocks(Map<String, Integer> keyQuantityPair) {
        List<Object> result = (List<Object>) executeScript(redisTemplate, setScript(), keyQuantityPair);
        Long statusCode = (Long) result.get(0);
        log.debug("상품 정보 캐시 등록 완료");
        return ResultCode.from(statusCode);
    }

    public RedisSubtractResult subtractMultipleStocks(Map<String, Integer> keyQuantityPair) {
        List<Object> redisResponse = (List<Object>) executeScript(redisTemplate, subtractScript(), keyQuantityPair);
        ResultCode resultCode = ResultCode.from((Long) redisResponse.get(0));
        List<Long> soldOutIds = convertKeysToIds((List<String>) redisResponse.get(1));
        List<Long> notFoundIds = convertKeysToIds((List<String>) redisResponse.get(2));

        try {
            RedisSubtractResult result = RedisSubtractResult.from(resultCode, soldOutIds, notFoundIds);
            if (result.isSoldOut() || result.isNotFound()) {
                if (result.isSoldOut()) {
                    log.debug("품절된 상품이 존재합니다. - {}", result.getSoldOutIds().toString());
                } else {
                    log.debug("캐시 저장소에 상품 정보가 존재하지 않습니다. - {}", result.getNotFoundIds().toString());
                }
                rollback(keyQuantityPair, soldOutIds, notFoundIds);
            }
            return result;
        } catch (Exception e) {
            log.debug(e.getMessage());
            return RedisSubtractResult.from(ResultCode.FAIL);
        }
    }

    private List<Long> convertKeysToIds(List<String> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }
        return keys.stream()
                .map(s -> Long.valueOf(s.replace("stock:", "")))
                .toList();
    }

    private void rollback(Map<String, Integer> keyQuantityPair, List<Long> soldOutIds, List<Long> notFoundIds) {
        Set<Long> failedIds = new HashSet<>();
        failedIds.addAll(soldOutIds);
        failedIds.addAll(notFoundIds);
        Map<String, Integer> successItemsMap = keyQuantityPair.entrySet().stream()
                .filter(entry -> !failedIds.contains(Long.valueOf(entry.getKey().replace("stock:", ""))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        executeScript(redisTemplate, rollbackScript(), successItemsMap);
    }

    private Object executeScript(RedisOperations<String, Object> operations, String luaScript, Map<String, Integer> keyValuePair) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(List.class);

        List<String> keys = keyValuePair.keySet().stream().toList();
        List<String> args = keyValuePair.values().stream().map(String::valueOf).toList();

        return operations.execute(script, keys, args.toArray());
    }

    private String subtractScript() {
        return """
                local statusCode = 0;
                local soldOutIds = {};
                local notFoundIds = {};
                for i, key in ipairs(KEYS) do
                    if (redis.call('exists', key) == 0) then
                        statusCode = -1;
                        table.insert(notFoundIds, key);
                    elseif (tonumber(redis.call('get', key)) - tonumber(ARGV[1]) >= 0) then
                        redis.call('decrby', key, ARGV[1]);
                        statusCode = 1;
                    else
                        statusCode = -1;
                        table.insert(soldOutIds, key);
                    end
                end
                return {statusCode, soldOutIds, notFoundIds};
                """.trim();
    }

    private String rollbackScript() {
        return """
                for i, key in ipairs(KEYS) do
                    if (redis.call('exists', key) == 1)
                    then
                        redis.call('increby', key, ARGV[1])
                    end
                end
                return 1;
                """.trim();
    }

    private String setScript() {
        return """
                for i, key in ipairs(KEYS) do
                    if (redis.call('exists', key) == 0)
                    then
                        redis.call('set', key, ARGV[1])
                    end
                end
                return 1;
                """.trim();
    }

}
