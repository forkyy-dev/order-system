package com.ordersystem.stock.application;

import com.ordersystem.category.domain.Category;
import com.ordersystem.category.domain.CategoryRepository;
import com.ordersystem.category.exception.CategoryNotFoundException;
import com.ordersystem.common.dto.PageDto;
import com.ordersystem.stock.application.dto.*;
import com.ordersystem.stock.domain.Stock;
import com.ordersystem.stock.domain.StockRepository;
import com.ordersystem.stock.events.StockEventProducer;
import com.ordersystem.stock.exception.DuplicatedStockException;
import com.ordersystem.stock.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockService {

    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final StockEventProducer stockEventProducer;

    @Transactional
    public StockDto create(final CreateStockDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));

        if (stockRepository.existsByNameAndCategoryId(dto.getName(), dto.getCategoryId())) {
            throw new DuplicatedStockException(dto.getName());
        }

        Stock stock = stockRepository.save(dto.toEntity());
        stockEventProducer.produceStockCacheUpdateEvent(Set.of(stock.getId()));
        return StockDto.from(stock, category);
    }

    @Transactional
    public StockDto modify(final ModifyStockDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
        Stock stock = stockRepository.findById(dto.getId())
                .orElseThrow(() -> new StockNotFoundException(dto.getId()));

        stock.updateInfo(dto.getName(), dto.getPrice(), dto.getQuantity(), dto.getCategoryId());
        return StockDto.from(stock, category);
    }

    @Transactional
    public void delete(final Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        stockRepository.delete(stock);
    }

    @Transactional(readOnly = true)
    public StockPaginationDto search(final SearchStockDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
        Slice<Stock> sliceResult = stockRepository.findByCategoryIdAndNameContains(dto.getCategoryId(), dto.getName(), dto.getPageable());

        if (!sliceResult.hasContent()) {
            return new StockPaginationDto(List.of(), PageDto.of(sliceResult.getSize(), sliceResult.getPageable().getPageNumber(), sliceResult.isLast()));
        }

        List<StockDto> stockDtos = sliceResult.getContent().stream()
                .map(stock -> StockDto.from(stock, category))
                .toList();

        return new StockPaginationDto(stockDtos, PageDto.of(sliceResult.getNumberOfElements(), sliceResult.getPageable().getPageNumber(), sliceResult.isLast()));
    }
}

