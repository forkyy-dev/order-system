package com.ordersystem.order.infra;

import com.ordersystem.order.domain.StockUpdateCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StockCustomRepository {

    private final EntityManager entityManager;

    @Transactional
    public int updateStockQuantities(Map<Long, Integer> stockIdQuantityPair, StockUpdateCondition condition) {
        if (stockIdQuantityPair.isEmpty()) {
            return 0;
        }

        StringBuilder queryBuilder = new StringBuilder("""
                UPDATE Stock s
                SET s.quantity = CASE
                """);

        stockIdQuantityPair.forEach((id, quantity) ->
                queryBuilder.append(
                        switch (condition) {
                            case INCREASE -> """
                                    WHEN s.id = %d THEN s.quantity + %d
                                    """.formatted(id, quantity);
                            case SUBTRACT -> """
                                    WHEN s.id = %d AND s.quantity - %d >= 0 THEN s.quantity - %d
                                    """.formatted(id, quantity, quantity);
                        })
        );

        String stockIds = stockIdQuantityPair.keySet().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        queryBuilder.append("""
                ELSE s.quantity END
                WHERE s.id IN (%s)
                """.formatted(stockIds));
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        return query.executeUpdate();
    }

}
